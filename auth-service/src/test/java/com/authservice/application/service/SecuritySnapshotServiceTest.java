package com.authservice.application.service;

import com.authservice.application.dto.SecuritySnapshotUpdate;
import com.authservice.application.port.out.ProcessedAuthEventStore;
import com.authservice.application.port.out.PasswordHasher;
import com.authservice.domain.model.AccountStatus;
import com.authservice.domain.model.AuthAccount;
import com.authservice.domain.repository.AuthAccountRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SecuritySnapshotServiceTest {
    @Test
    void synchronizesRoleFacultyAndStatusOnlyOncePerEvent() {
        AuthAccountRepository accounts = mock(AuthAccountRepository.class);
        ProcessedAuthEventStore inbox = mock(ProcessedAuthEventStore.class);
        SecuritySnapshotService service = new SecuritySnapshotService(accounts, inbox);
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        AuthAccount account = new AuthAccount(userId, "GV001", "hash", AccountStatus.PENDING_APPROVAL,
                "USER", null, "old@hau.edu.vn", Instant.now(), Instant.now(), 0);
        when(accounts.findById(userId)).thenReturn(Optional.of(account));
        when(accounts.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        boolean changed = service.synchronize(new SecuritySnapshotUpdate(eventId, "USER_APPROVED", 1,
                userId, "GV001", "ACTIVE", "SUBJECT_ADMIN", "CNTT", "new@hau.edu.vn"));

        assertThat(changed).isTrue();
        var saved = org.mockito.ArgumentCaptor.forClass(AuthAccount.class);
        verify(accounts).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(saved.getValue().getRole()).isEqualTo("SUBJECT_ADMIN");
        assertThat(saved.getValue().getFacultyId()).isEqualTo("CNTT");
        verify(inbox).record(eq(eventId), eq("USER_APPROVED"), any());

        when(inbox.exists(eventId)).thenReturn(true);
        assertThat(service.synchronize(new SecuritySnapshotUpdate(eventId, "USER_APPROVED", 1,
                userId, "GV001", "ACTIVE", "SUBJECT_ADMIN", "CNTT", "new@hau.edu.vn"))).isFalse();
        verify(accounts, times(1)).save(any());
    }

    @Test
    void provisionsImportedAccountWithoutTransportingPlaintextPassword() {
        AuthAccountRepository accounts = mock(AuthAccountRepository.class);
        ProcessedAuthEventStore inbox = mock(ProcessedAuthEventStore.class);
        PasswordHasher hasher = mock(PasswordHasher.class);
        when(hasher.hash(any())).thenReturn("secure-random-hash");
        when(accounts.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        var service = new SecuritySnapshotService(accounts, inbox, hasher);
        boolean changed = service.synchronize(new SecuritySnapshotUpdate(eventId,
                "USER_ACCOUNT_IMPORT_REQUESTED", 1, userId, "GV100", "PENDING_APPROVAL",
                "USER", "CNTT", "gv100@hau.edu.vn"));

        assertThat(changed).isTrue();
        verify(hasher).hash(any());
        var saved = org.mockito.ArgumentCaptor.forClass(AuthAccount.class);
        verify(accounts, times(2)).save(saved.capture());
        assertThat(saved.getAllValues().getFirst().getPasswordHash()).isEqualTo("secure-random-hash");
        assertThat(saved.getAllValues().getLast().getStatus()).isEqualTo(AccountStatus.PENDING_APPROVAL);
        verify(inbox).record(eq(eventId), eq("USER_ACCOUNT_IMPORT_REQUESTED"), any());
    }
}
