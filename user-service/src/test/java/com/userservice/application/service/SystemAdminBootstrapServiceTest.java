package com.userservice.application.service;

import com.userservice.application.port.out.ProcessedEventStore;
import com.userservice.domain.model.Role;
import com.userservice.domain.model.UserStatus;
import com.userservice.domain.repository.UserProfileRepository;
import com.userservice.infrastructure.rabbitmq.contract.BootstrapAdminRequestedPayload;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SystemAdminBootstrapServiceTest {
    @Test void createsActiveSystemAdminProfile() {
        var repo = mock(UserProfileRepository.class); var events = mock(ProcessedEventStore.class);
        var service = new SystemAdminBootstrapService(repo, events, Clock.fixed(Instant.parse("2026-09-07T00:00:00Z"), ZoneOffset.UTC));
        var payload = payload(); UUID eventId = UUID.randomUUID();
        assertThat(service.createIfAbsent(eventId, payload)).isTrue();
        verify(repo).save(argThat(p -> p.getId().equals(payload.userId()) && p.getRole() == Role.SYSTEM_ADMIN && p.getStatus() == UserStatus.ACTIVE));
        verify(events).record(eq(eventId), eq(SystemAdminBootstrapService.EVENT_TYPE), any());
    }

    @Test void duplicateEventAndExistingProfileDoNotCreateAnotherProfile() {
        var repo = mock(UserProfileRepository.class); var events = mock(ProcessedEventStore.class);
        var service = new SystemAdminBootstrapService(repo, events, Clock.systemUTC());
        var payload = payload(); UUID eventId = UUID.randomUUID();
        when(events.exists(eventId)).thenReturn(true);
        assertThat(service.createIfAbsent(eventId, payload)).isFalse();
        verifyNoInteractions(repo);
        when(events.exists(eventId)).thenReturn(false); when(repo.existsById(payload.userId())).thenReturn(true);
        assertThat(service.createIfAbsent(eventId, payload)).isFalse();
        verify(repo, never()).save(any());
    }

    private BootstrapAdminRequestedPayload payload() {
        return new BootstrapAdminRequestedPayload(UUID.randomUUID(), "ADMIN001", "admin@example.local", "System Administrator", Role.SYSTEM_ADMIN, UserStatus.ACTIVE, null);
    }
}
