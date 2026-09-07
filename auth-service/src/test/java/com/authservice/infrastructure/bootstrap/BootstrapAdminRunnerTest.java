package com.authservice.infrastructure.bootstrap;

import com.authservice.application.port.out.AuthEventPublisher;
import com.authservice.application.port.out.PasswordHasher;
import com.authservice.domain.model.AccountStatus;
import com.authservice.domain.model.AuthAccount;
import com.authservice.domain.repository.AuthAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BootstrapAdminRunnerTest {
    private final AuthAccountRepository accounts = mock(AuthAccountRepository.class);
    private final PasswordHasher hasher = mock(PasswordHasher.class);
    private final AuthEventPublisher events = mock(AuthEventPublisher.class);
    private final BootstrapAdminProperties properties = properties();
    private final BootstrapAdminRunner runner = new BootstrapAdminRunner(properties, accounts, hasher, events,
            Clock.fixed(Instant.parse("2026-09-07T00:00:00Z"), ZoneOffset.UTC));

    @Test void createsActiveSystemAdminWithEncodedPassword() {
        when(accounts.findByLecturerCode("ADMIN001")).thenReturn(Optional.empty());
        when(accounts.findBySecurityEmail("admin@example.local")).thenReturn(Optional.empty());
        when(hasher.hash("local-secret" )).thenReturn("encoded");
        runner.run(new DefaultApplicationArguments(new String[0]));
        var captor = org.mockito.ArgumentCaptor.forClass(AuthAccount.class);
        verify(accounts).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo("SYSTEM_ADMIN");
        assertThat(captor.getValue().getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("encoded");
        verify(events).publish(eq("USER_BOOTSTRAP_ADMIN_REQUESTED"), eq("user.bootstrap-admin.requested"), any(), anyMap());
    }

    @Test void existingAdminIsNotOverwrittenButSynchronizationCanRecoverProfile() {
        AuthAccount existing = AuthAccount.bootstrapAdmin(UUID.randomUUID(), "ADMIN001", "existing-hash", "admin@example.local", null, Instant.now());
        when(accounts.findByLecturerCode("ADMIN001")).thenReturn(Optional.of(existing));
        runner.run(new DefaultApplicationArguments(new String[0]));
        verify(accounts, never()).save(any());
        verify(hasher, never()).hash(any());
        verify(events).publish(eq("USER_BOOTSTRAP_ADMIN_REQUESTED"), eq("user.bootstrap-admin.requested"), eq(existing.getId()), anyMap());
    }

    @Test void disabledBootstrapDoesNothing() {
        properties.setEnabled(false);
        runner.run(new DefaultApplicationArguments(new String[0]));
        verifyNoInteractions(accounts, hasher, events);
    }

    @Test void enabledBootstrapWithoutPasswordFailsWithoutLoggingSecret() {
        properties.setPassword("");
        assertThatThrownBy(() -> runner.run(new DefaultApplicationArguments(new String[0])))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("required");
        verifyNoInteractions(accounts, hasher, events);
    }

    private static BootstrapAdminProperties properties() {
        var p = new BootstrapAdminProperties();
        p.setEnabled(true); p.setLecturerCode("ADMIN001"); p.setEmail("admin@example.local");
        p.setFullName("System Administrator"); p.setPassword("local-secret");
        return p;
    }
}
