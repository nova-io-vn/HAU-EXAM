package com.authservice.application.service;

import com.authservice.application.dto.AuthDtos;
import com.authservice.application.port.out.AuthEventPublisher;
import com.authservice.application.port.out.OtpStore;
import com.authservice.application.port.out.PasswordHasher;
import com.authservice.application.port.out.RefreshTokenStore;
import com.authservice.application.port.out.TokenService;
import com.authservice.domain.exception.AuthAccountNotFoundException;
import com.authservice.domain.exception.DomainException;
import com.authservice.domain.exception.LecturerCodeAlreadyExistsException;
import com.authservice.domain.model.AccountStatus;
import com.authservice.domain.model.AuthAccount;
import com.authservice.domain.repository.AuthAccountRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthApplicationService {
    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AuthAccountRepository accounts;
    private final PasswordHasher passwordHasher;
    private final TokenService tokens;
    private final RefreshTokenStore refreshTokens;
    private final OtpStore otpStore;
    private final AuthEventPublisher events;

    public AuthApplicationService(AuthAccountRepository accounts, PasswordHasher passwordHasher,
                                 TokenService tokens, RefreshTokenStore refreshTokens,
                                 OtpStore otpStore, AuthEventPublisher events) {
        this.accounts = accounts;
        this.passwordHasher = passwordHasher;
        this.tokens = tokens;
        this.refreshTokens = refreshTokens;
        this.otpStore = otpStore;
        this.events = events;
    }

    public AuthDtos.Registration register(AuthDtos.RegistrationInput input, UUID correlationId) {
        if (accounts.existsByLecturerCode(input.lecturerCode())) throw new LecturerCodeAlreadyExistsException();
        Instant now = Instant.now();
        AuthAccount account = accounts.save(AuthAccount.pending(input.lecturerCode(), passwordHasher.hash(input.password()),
                input.email(), input.facultyId(), now));
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", account.getId());
        payload.put("lecturerCode", account.getLecturerCode());
        payload.put("fullName", input.fullName());
        payload.put("dateOfBirth", input.dateOfBirth());
        payload.put("phone", input.phone());
        payload.put("email", input.email());
        payload.put("address", input.address());
        payload.put("avatar", input.avatar());
        payload.put("facultyId", input.facultyId());
        events.publish("USER_REGISTRATION_REQUESTED", "user.registration.requested",
                correlationId, payload);
        return new AuthDtos.Registration(account.getStatus());
    }

    public AuthDtos.Session login(String lecturerCode, String password) {
        AuthAccount account = accounts.findByLecturerCode(lecturerCode)
                .orElseThrow(() -> new AuthException("INVALID_CREDENTIALS", "Invalid credentials"));
        if (account.getStatus() == AccountStatus.PENDING_APPROVAL)
            throw new AuthException("ACCOUNT_PENDING_APPROVAL", "Account is pending approval");
        if (account.getStatus() == AccountStatus.REJECTED)
            throw new AuthException("ACCOUNT_REJECTED", "Account was rejected");
        if (account.getStatus() == AccountStatus.LOCKED)
            throw new AuthException("ACCOUNT_LOCKED", "Account is locked");
        if (!passwordHasher.matches(password, account.getPasswordHash()))
            throw new AuthException("INVALID_CREDENTIALS", "Invalid credentials");
        return issueSession(account);
    }

    public AuthDtos.Session refresh(String refreshToken) {
        TokenService.RefreshClaims claims = tokens.parseRefreshToken(refreshToken);
        RefreshTokenStore.StoredRefreshToken stored = refreshTokens.find(claims.tokenId())
                .filter(value -> value.isUsable(Instant.now()))
                .filter(value -> passwordHasher.matches(refreshToken, value.tokenHash()))
                .orElseThrow(() -> new AuthException("INVALID_REFRESH_TOKEN", "Refresh token is invalid"));
        AuthAccount account = accounts.findById(stored.accountId())
                .orElseThrow(() -> new AuthAccountNotFoundException(stored.accountId()));
        if (!account.canAuthenticate()) throw new AuthException("ACCOUNT_NOT_ACTIVE", "Account is not active");
        refreshTokens.revoke(claims.tokenId(), Instant.now());
        return issueSession(account);
    }

    public void logout(String refreshToken) {
        TokenService.RefreshClaims claims = tokens.parseRefreshToken(refreshToken);
        refreshTokens.revoke(claims.tokenId(), Instant.now());
    }

    public AuthDtos.Accepted forgotPassword(String lecturerCode, UUID correlationId) {
        accounts.findByLecturerCode(lecturerCode).ifPresent(account -> {
            if (account.getSecurityEmail() == null) return;
            String otp = "%06d".formatted(RANDOM.nextInt(1_000_000));
            Instant expiresAt = Instant.now().plus(OTP_TTL);
            otpStore.save(account.getLecturerCode(), passwordHasher.hash(otp), expiresAt);
            Map<String, Object> payload = new HashMap<>();
            payload.put("recipientUserId", account.getId());
            payload.put("email", account.getSecurityEmail());
            payload.put("otp", otp);
            payload.put("expiresAt", expiresAt);
            events.publish("PASSWORD_RESET_OTP_REQUESTED", "password.reset.otp.requested",
                    correlationId, payload);
        });
        return new AuthDtos.Accepted("OTP_REQUESTED");
    }

    public AuthDtos.OtpVerification verifyOtp(String lecturerCode, String otp) {
        OtpStore.Verification result = otpStore.verify(normalize(lecturerCode), otp);
        if (result != OtpStore.Verification.VALID)
            throw new AuthException(result == OtpStore.Verification.EXPIRED ? "OTP_EXPIRED" : "INVALID_OTP", "OTP is invalid or expired");
        otpStore.markVerified(normalize(lecturerCode), Instant.now().plus(OTP_TTL));
        return new AuthDtos.OtpVerification(true, null);
    }

    public void resetPassword(String lecturerCode, String otp, String newPassword) {
        String identity = normalize(lecturerCode);
        OtpStore.Verification result = otpStore.verify(identity, otp);
        if (result != OtpStore.Verification.VALID)
            throw new AuthException("INVALID_RESET_AUTHORIZATION", "Reset authorization is invalid");
        AuthAccount account = accounts.findByLecturerCode(identity)
                .orElseThrow(() -> new AuthAccountNotFoundException(UUID.nameUUIDFromBytes(identity.getBytes())));
        accounts.save(account.changePasswordHash(passwordHasher.hash(newPassword), Instant.now()));
        refreshTokens.revokeAllForAccount(account.getId(), Instant.now());
        otpStore.invalidate(identity);
    }

    private AuthDtos.Session issueSession(AuthAccount account) {
        TokenService.IssuedTokens issued = tokens.issue(account);
        refreshTokens.save(issued.refreshTokenId(), account.getId(), passwordHasher.hash(issued.refreshToken()), issued.refreshExpiresAt());
        return new AuthDtos.Session(account.getId(), account.getLecturerCode(), account.getRole(), account.getFacultyId(),
                issued.accessToken(), issued.refreshToken(), issued.accessExpiresAt(), issued.refreshExpiresAt());
    }

    private String normalize(String lecturerCode) { return lecturerCode == null ? null : lecturerCode.trim().toUpperCase(); }

    public static final class AuthException extends DomainException {
        public AuthException(String code, String message) { super(code, message); }
    }
}
