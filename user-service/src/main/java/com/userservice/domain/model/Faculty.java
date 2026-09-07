package com.userservice.domain.model;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public record Faculty(UUID id, String code, String name, String description, boolean active, Instant createdAt, Instant updatedAt) {
    public Faculty {
        Objects.requireNonNull(id); Objects.requireNonNull(createdAt); Objects.requireNonNull(updatedAt);
        if (code == null || code.isBlank() || code.trim().length() > 50) throw new IllegalArgumentException("Faculty code is required");
        if (name == null || name.isBlank() || name.trim().length() > 255) throw new IllegalArgumentException("Faculty name is required");
        code = code.trim().toUpperCase(Locale.ROOT); name = name.trim();
        if (updatedAt.isBefore(createdAt)) throw new IllegalArgumentException("updatedAt must not be before createdAt");
    }
}
