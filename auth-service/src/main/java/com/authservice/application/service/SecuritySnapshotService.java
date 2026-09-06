package com.authservice.application.service;

import com.authservice.application.dto.SecuritySnapshotUpdate;
import com.authservice.application.port.out.ProcessedAuthEventStore;
import com.authservice.domain.exception.AuthAccountNotFoundException;
import com.authservice.domain.model.AccountStatus;
import com.authservice.domain.repository.AuthAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class SecuritySnapshotService {
    private static final int SUPPORTED_EVENT_VERSION = 1;

    private final AuthAccountRepository accounts;
    private final ProcessedAuthEventStore processedEvents;

    public SecuritySnapshotService(AuthAccountRepository accounts, ProcessedAuthEventStore processedEvents) {
        this.accounts = accounts;
        this.processedEvents = processedEvents;
    }

    @Transactional
    public boolean synchronize(SecuritySnapshotUpdate update) {
        validate(update);
        if (processedEvents.exists(update.eventId())) {
            return false;
        }

        var account = accounts.findById(update.userId())
                .orElseThrow(() -> new AuthAccountNotFoundException(update.userId()));
        if (!account.getLecturerCode().equalsIgnoreCase(update.lecturerCode())) {
            throw new IllegalArgumentException("Security snapshot lecturerCode does not match account");
        }

        AccountStatus status = AccountStatus.valueOf(update.status());
        accounts.save(account.synchronize(status, update.role(), update.facultyId(), update.email(), Instant.now()));
        processedEvents.record(update.eventId(), update.eventType(), Instant.now());
        return true;
    }

    private void validate(SecuritySnapshotUpdate update) {
        if (update == null || update.eventId() == null || update.eventType() == null
                || update.userId() == null || update.lecturerCode() == null
                || update.status() == null || update.role() == null) {
            throw new IllegalArgumentException("Invalid user security snapshot event");
        }
        if (update.eventVersion() != SUPPORTED_EVENT_VERSION) {
            throw new IllegalArgumentException("Unsupported user security snapshot event version");
        }
    }
}
