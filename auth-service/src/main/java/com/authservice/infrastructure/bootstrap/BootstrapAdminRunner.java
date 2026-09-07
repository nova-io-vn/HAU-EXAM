package com.authservice.infrastructure.bootstrap;

import com.authservice.application.port.out.AuthEventPublisher;
import com.authservice.application.port.out.PasswordHasher;
import com.authservice.domain.model.AccountStatus;
import com.authservice.domain.model.AuthAccount;
import com.authservice.domain.repository.AuthAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class BootstrapAdminRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(BootstrapAdminRunner.class);
    private final BootstrapAdminProperties properties;
    private final AuthAccountRepository accounts;
    private final PasswordHasher passwordHasher;
    private final AuthEventPublisher events;
    private final Clock clock;
    public BootstrapAdminRunner(BootstrapAdminProperties properties, AuthAccountRepository accounts, PasswordHasher passwordHasher, AuthEventPublisher events, Clock clock) {
        this.properties = properties; this.accounts = accounts; this.passwordHasher = passwordHasher; this.events = events; this.clock = clock;
    }
    @Override public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) return;
        validate();
        var existing = accounts.findByLecturerCode(properties.getLecturerCode()).or(() -> accounts.findBySecurityEmail(properties.getEmail()));
        if (existing.isPresent()) {
            AuthAccount account = existing.get();
            if (account.getStatus() == AccountStatus.ACTIVE && "SYSTEM_ADMIN".equals(account.getRole())) {
                publish(account.getId(), account.getLecturerCode(), account.getSecurityEmail(), properties.getFullName(), account.getFacultyId());
                log.info("Bootstrap SYSTEM_ADMIN already exists; synchronization event published");
            } else log.warn("Bootstrap identity already exists with a non-admin state; no account was changed");
            return;
        }
        Instant now = Instant.now(clock);
        AuthAccount account = AuthAccount.bootstrapAdmin(UUID.randomUUID(), properties.getLecturerCode(), passwordHasher.hash(properties.getPassword()), properties.getEmail(), properties.getFacultyId(), now);
        accounts.save(account);
        publish(account.getId(), account.getLecturerCode(), account.getSecurityEmail(), properties.getFullName(), account.getFacultyId());
        log.info("Bootstrap SYSTEM_ADMIN created and synchronization event published");
    }
    private void publish(UUID userId, String lecturerCode, String email, String fullName, String facultyId) {
        events.publish("USER_BOOTSTRAP_ADMIN_REQUESTED", "user.bootstrap-admin.requested", userId,
                Map.of("userId", userId, "lecturerCode", lecturerCode, "email", email, "fullName", fullName,
                        "role", "SYSTEM_ADMIN", "status", "ACTIVE", "facultyId", facultyId == null ? "" : facultyId));
    }
    private void validate() {
        if (blank(properties.getLecturerCode()) || blank(properties.getEmail()) || blank(properties.getFullName()) || blank(properties.getPassword()))
            throw new IllegalStateException("Bootstrap admin is enabled but lecturer code, email, full name, and password are required");
        if (properties.getPassword().length() < 8) throw new IllegalStateException("Bootstrap admin password must contain at least 8 characters");
    }
    private boolean blank(String value) { return value == null || value.isBlank(); }
}
