package com.authservice.application.service;

import com.authservice.application.dto.SecuritySnapshotUpdate;
import com.authservice.application.port.out.ProcessedAuthEventStore;
import com.authservice.application.port.out.PasswordHasher;
import com.authservice.domain.exception.AuthAccountNotFoundException;
import com.authservice.domain.model.AccountStatus;
import com.authservice.domain.repository.AuthAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class SecuritySnapshotService {
    private static final int SUPPORTED_EVENT_VERSION = 1;

    private final AuthAccountRepository accounts;
    private final ProcessedAuthEventStore processedEvents;
    private final PasswordHasher passwordHasher;
    private static final SecureRandom RANDOM = new SecureRandom();

    public SecuritySnapshotService(AuthAccountRepository accounts, ProcessedAuthEventStore processedEvents) {
        this(accounts, processedEvents, null);
    }

    @Autowired
    public SecuritySnapshotService(AuthAccountRepository accounts, ProcessedAuthEventStore processedEvents, PasswordHasher passwordHasher) {
        this.accounts = accounts;
        this.processedEvents = processedEvents;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    public boolean synchronize(SecuritySnapshotUpdate update) {
        validate(update);
        if (processedEvents.exists(update.eventId())) {
            return false;
        }

        var account = accounts.findById(update.userId()).orElseGet(() -> provisionImportedAccount(update));
        if (!account.getLecturerCode().equalsIgnoreCase(update.lecturerCode())) {
            throw new IllegalArgumentException("Security snapshot lecturerCode does not match account");
        }

        AccountStatus status = AccountStatus.valueOf(update.status());
        accounts.save(account.synchronize(status, update.role(), update.facultyId(), update.email(), Instant.now()));
        processedEvents.record(update.eventId(), update.eventType(), Instant.now());
        return true;
    }

    private com.authservice.domain.model.AuthAccount provisionImportedAccount(SecuritySnapshotUpdate update) {
        if (!"USER_ACCOUNT_IMPORT_REQUESTED".equals(update.eventType()) || passwordHasher == null) throw new AuthAccountNotFoundException(update.userId());
        if (accounts.existsByLecturerCode(update.lecturerCode())) throw new IllegalArgumentException("Imported lecturerCode already exists in Auth Service");
        byte[] secret = new byte[32]; RANDOM.nextBytes(secret);
        return accounts.save(com.authservice.domain.model.AuthAccount.importedPending(update.userId(), update.lecturerCode(),
                passwordHasher.hash(Base64.getUrlEncoder().withoutPadding().encodeToString(secret)), update.email(), update.facultyId(), Instant.now()));
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
