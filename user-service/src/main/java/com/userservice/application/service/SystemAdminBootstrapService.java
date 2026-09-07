package com.userservice.application.service;

import com.userservice.application.port.out.ProcessedEventStore;
import com.userservice.domain.model.Role;
import com.userservice.domain.model.UserProfile;
import com.userservice.domain.model.UserStatus;
import com.userservice.domain.repository.UserProfileRepository;
import com.userservice.infrastructure.rabbitmq.contract.BootstrapAdminRequestedPayload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class SystemAdminBootstrapService {
    public static final String EVENT_TYPE = "USER_BOOTSTRAP_ADMIN_REQUESTED";
    private final UserProfileRepository repository;
    private final ProcessedEventStore events;
    private final Clock clock;
    public SystemAdminBootstrapService(UserProfileRepository repository, ProcessedEventStore events, Clock clock) {
        this.repository = repository; this.events = events; this.clock = clock;
    }
    @Transactional
    public boolean createIfAbsent(UUID eventId, BootstrapAdminRequestedPayload payload) {
        if (events.exists(eventId)) return false;
        if (payload.role() != Role.SYSTEM_ADMIN || payload.status() != UserStatus.ACTIVE)
            throw new IllegalArgumentException("Bootstrap event must create an active SYSTEM_ADMIN");
        if (repository.existsById(payload.userId()) || repository.existsByLecturerCode(payload.lecturerCode())) {
            events.record(eventId, EVENT_TYPE, Instant.now(clock));
            return false;
        }
        if (repository.existsByEmail(payload.email())) throw new IllegalStateException("Bootstrap admin email is already assigned to another profile");
        Instant now = Instant.now(clock);
        repository.save(UserProfile.bootstrapAdmin(payload.userId(), payload.lecturerCode(), payload.fullName(), payload.email(), payload.facultyId(), now));
        events.record(eventId, EVENT_TYPE, now);
        return true;
    }
}
