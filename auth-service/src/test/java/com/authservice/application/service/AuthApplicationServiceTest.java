package com.authservice.application.service;

import com.authservice.application.dto.AuthDtos;
import com.authservice.application.port.out.*;
import com.authservice.domain.model.AccountStatus;
import com.authservice.domain.model.AuthAccount;
import com.authservice.domain.repository.AuthAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthApplicationServiceTest {
    @Mock AuthAccountRepository accounts;
    @Mock PasswordHasher hasher;
    @Mock TokenService tokenService;
    @Mock RefreshTokenStore refreshTokens;
    @Mock OtpStore otpStore;
    @Mock AuthEventPublisher events;
    private AuthApplicationService service;

    @BeforeEach void setUp() { service = new AuthApplicationService(accounts, hasher, tokenService, refreshTokens, otpStore, events); }

    @Test void registerCreatesPendingCredentialAndPublishesEvent() {
        when(accounts.existsByLecturerCode("gv001")).thenReturn(false);
        when(hasher.hash("password123")).thenReturn("hash");
        when(accounts.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        UUID correlationId = UUID.randomUUID();
        AuthDtos.Registration result = service.register(registrationInput(), correlationId);
        assertThat(result.status()).isEqualTo(AccountStatus.PENDING_APPROVAL);
        @SuppressWarnings("unchecked")
        var payload = org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(events).publish(eq("USER_REGISTRATION_REQUESTED"), eq("user.registration.requested"),
                eq(correlationId), payload.capture());
        assertThat(payload.getValue()).containsKeys("userId", "lecturerCode", "fullName", "email")
                .doesNotContainKeys("password", "passwordHash", "accountId");
    }

    @Test void duplicateLecturerCodeIsRejected() {
        when(accounts.existsByLecturerCode("GV001")).thenReturn(true);
        assertThatThrownBy(() -> service.register(registrationInput(), UUID.randomUUID()))
                .hasFieldOrPropertyWithValue("code", "LECTURER_CODE_ALREADY_EXISTS");
    }

    @Test void pendingAccountCannotLogin() {
        AuthAccount account = account(AccountStatus.PENDING_APPROVAL);
        when(accounts.findByLecturerCode("GV001")).thenReturn(Optional.of(account));
        assertThatThrownBy(() -> service.login("GV001", "password123"))
                .hasMessage("Account is pending approval").hasFieldOrPropertyWithValue("code", "ACCOUNT_PENDING_APPROVAL");
    }

    @Test void activeAccountCanLogin() {
        AuthAccount account = account(AccountStatus.ACTIVE);
        when(accounts.findByLecturerCode("GV001")).thenReturn(Optional.of(account));
        when(hasher.matches("password123", "hash")).thenReturn(true);
        TokenService.IssuedTokens issued = new TokenService.IssuedTokens("access", "refresh", UUID.randomUUID(), Instant.now().plusSeconds(60), Instant.now().plusSeconds(3600));
        when(tokenService.issue(account)).thenReturn(issued);
        when(hasher.hash("refresh")).thenReturn("refresh-hash");
        AuthDtos.Session session = service.login("GV001", "password123");
        assertThat(session.accessToken()).isEqualTo("access");
        verify(refreshTokens).save(any(), eq(account.getId()), eq("refresh-hash"), eq(issued.refreshExpiresAt()));
    }

    @Test void wrongPasswordIsInvalidCredentials() {
        AuthAccount account = account(AccountStatus.ACTIVE);
        when(accounts.findByLecturerCode("GV001")).thenReturn(Optional.of(account));
        when(hasher.matches("wrong", "hash")).thenReturn(false);
        assertThatThrownBy(() -> service.login("GV001", "wrong"))
                .hasFieldOrPropertyWithValue("code", "INVALID_CREDENTIALS");
    }

    @Test void rejectedAndLockedAccountsAreDistinguished() {
        for (AccountStatus status : new AccountStatus[]{AccountStatus.REJECTED, AccountStatus.LOCKED}) {
            AuthAccount account = account(status);
            when(accounts.findByLecturerCode("GV001")).thenReturn(Optional.of(account));
            assertThatThrownBy(() -> service.login("GV001", "password123"))
                    .hasFieldOrPropertyWithValue("code", status == AccountStatus.REJECTED ? "ACCOUNT_REJECTED" : "ACCOUNT_LOCKED");
        }
    }

    @Test void validRefreshRotatesAndRevokesPreviousToken() {
        AuthAccount account = account(AccountStatus.ACTIVE);
        UUID tokenId = UUID.randomUUID();
        when(tokenService.parseRefreshToken("refresh")).thenReturn(new TokenService.RefreshClaims(account.getId(), tokenId, Instant.now().plusSeconds(60)));
        when(refreshTokens.find(tokenId)).thenReturn(Optional.of(new RefreshTokenStore.StoredRefreshToken(tokenId, account.getId(), "hash", Instant.now().plusSeconds(60), null)));
        when(hasher.matches("refresh", "hash")).thenReturn(true);
        when(accounts.findById(account.getId())).thenReturn(Optional.of(account));
        TokenService.IssuedTokens issued = new TokenService.IssuedTokens("new-access", "new-refresh", UUID.randomUUID(), Instant.now().plusSeconds(60), Instant.now().plusSeconds(3600));
        when(tokenService.issue(account)).thenReturn(issued);
        when(hasher.hash("new-refresh")).thenReturn("new-hash");
        assertThat(service.refresh("refresh").accessToken()).isEqualTo("new-access");
        verify(refreshTokens).revoke(eq(tokenId), any());
    }

    @Test void revokedRefreshIsRejected() {
        UUID tokenId = UUID.randomUUID();
        when(tokenService.parseRefreshToken("refresh")).thenReturn(new TokenService.RefreshClaims(UUID.randomUUID(), tokenId, Instant.now().plusSeconds(60)));
        when(refreshTokens.find(tokenId)).thenReturn(Optional.of(new RefreshTokenStore.StoredRefreshToken(tokenId, UUID.randomUUID(), "hash", Instant.now().plusSeconds(60), Instant.now())));
        assertThatThrownBy(() -> service.refresh("refresh")).hasFieldOrPropertyWithValue("code", "INVALID_REFRESH_TOKEN");
    }

    @Test void forgotPasswordReturnsGenericResultForUnknownAccount() {
        when(accounts.findByLecturerCode("UNKNOWN")).thenReturn(Optional.empty());
        assertThat(service.forgotPassword("UNKNOWN", UUID.randomUUID()).status()).isEqualTo("OTP_REQUESTED");
        verifyNoInteractions(otpStore, events);
    }

    @Test void forgotPasswordStoresOtpAndPublishesEventForExistingAccount() {
        AuthAccount account = account(AccountStatus.ACTIVE);
        when(accounts.findByLecturerCode("GV001")).thenReturn(Optional.of(account));
        when(hasher.hash(anyString())).thenReturn("otp-hash");
        UUID correlationId = UUID.randomUUID();
        assertThat(service.forgotPassword("GV001", correlationId).status()).isEqualTo("OTP_REQUESTED");
        verify(otpStore).save(eq("GV001"), eq("otp-hash"), any());
        @SuppressWarnings("unchecked")
        var payload = org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(events).publish(eq("PASSWORD_RESET_OTP_REQUESTED"), eq("password.reset.otp.requested"),
                eq(correlationId), payload.capture());
        assertThat(payload.getValue()).containsEntry("email", "gv001@hau.edu.vn").containsKey("otp")
                .doesNotContainKeys("password", "token");
    }

    @Test void invalidOtpIsRejected() {
        when(otpStore.verify("GV001", "123456")).thenReturn(OtpStore.Verification.INVALID);
        assertThatThrownBy(() -> service.verifyOtp("GV001", "123456"))
                .hasFieldOrPropertyWithValue("code", "INVALID_OTP");
    }

    @Test void validAndExpiredOtpAreHandled() {
        when(otpStore.verify("GV001", "123456")).thenReturn(OtpStore.Verification.VALID);
        assertThat(service.verifyOtp("GV001", "123456").verified()).isTrue();
        verify(otpStore).markVerified(eq("GV001"), any());
        when(otpStore.verify("GV001", "000000")).thenReturn(OtpStore.Verification.EXPIRED);
        assertThatThrownBy(() -> service.verifyOtp("GV001", "000000"))
                .hasFieldOrPropertyWithValue("code", "OTP_EXPIRED");
    }

    @Test void resetRevokesExistingRefreshTokens() {
        AuthAccount account = account(AccountStatus.ACTIVE);
        when(otpStore.verify("GV001", "123456")).thenReturn(OtpStore.Verification.VALID);
        when(accounts.findByLecturerCode("GV001")).thenReturn(Optional.of(account));
        when(hasher.hash("newpassword")).thenReturn("new-hash");
        when(accounts.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        service.resetPassword("GV001", "123456", "newpassword");
        verify(refreshTokens).revokeAllForAccount(eq(account.getId()), any());
        verify(otpStore).invalidate("GV001");
    }

    private AuthAccount account(AccountStatus status) {
        return new AuthAccount(UUID.randomUUID(), "GV001", "hash", status, "USER", "CNTT",
                "gv001@hau.edu.vn", Instant.now(), Instant.now(), 0);
    }

    private AuthDtos.RegistrationInput registrationInput() {
        return new AuthDtos.RegistrationInput("GV001", "password123", "Giang vien 1",
                LocalDate.of(1990, 1, 1), "0900000000", "gv001@hau.edu.vn",
                "Ha Noi", null, "CNTT");
    }
}
