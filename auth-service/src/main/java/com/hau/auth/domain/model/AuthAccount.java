package com.hau.auth.domain.model;

import com.hau.auth.domain.exception.InvalidAuthAccountException;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public final class AuthAccount {

    private static final int MAX_LECTURER_CODE_LENGTH = 50;
    private static final int MAX_PASSWORD_HASH_LENGTH = 100;

    private final UUID id;
    private final String lecturerCode;
    private final String passwordHash;
    private final AccountStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final long version;

    public AuthAccount(
            UUID id,
            String lecturerCode,
            String passwordHash,
            AccountStatus status,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.lecturerCode = normalizeLecturerCode(lecturerCode);
        this.passwordHash = requirePasswordHash(passwordHash);
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        if (updatedAt.isBefore(createdAt)) {
            throw new InvalidAuthAccountException("updatedAt must not be before createdAt");
        }
        if (version < 0) {
            throw new InvalidAuthAccountException("version must not be negative");
        }
        this.version = version;
    }

    public static AuthAccount pending(String lecturerCode, String passwordHash, Instant now) {
        return new AuthAccount(
                UUID.randomUUID(), lecturerCode, passwordHash,
                AccountStatus.PENDING_APPROVAL, now, now, 0
        );
    }

    public boolean canAuthenticate() {
        return status == AccountStatus.ACTIVE;
    }

    public AuthAccount changeStatus(AccountStatus newStatus, Instant changedAt) {
        return new AuthAccount(id, lecturerCode, passwordHash, newStatus, createdAt, changedAt, version);
    }

    public AuthAccount changePasswordHash(String newPasswordHash, Instant changedAt) {
        return new AuthAccount(id, lecturerCode, newPasswordHash, status, createdAt, changedAt, version);
    }

    private static String normalizeLecturerCode(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidAuthAccountException("lecturerCode must not be blank");
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() > MAX_LECTURER_CODE_LENGTH) {
            throw new InvalidAuthAccountException("lecturerCode must not exceed 50 characters");
        }
        return normalized;
    }

    private static String requirePasswordHash(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidAuthAccountException("passwordHash must not be blank");
        }
        if (value.length() > MAX_PASSWORD_HASH_LENGTH) {
            throw new InvalidAuthAccountException("passwordHash must not exceed 100 characters");
        }
        return value;
    }

    public UUID getId() { return id; }
    public String getLecturerCode() { return lecturerCode; }
    public String getPasswordHash() { return passwordHash; }
    public AccountStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }
}
