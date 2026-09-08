package com.userservice.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class UserOnboardingState {
    private final UUID id;
    private final UUID userId;
    private final Role role;
    private final int versionCompleted;
    private final Instant completedAt;

    public UserOnboardingState(UUID id, UUID userId, Role role, int versionCompleted, Instant completedAt) {
        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId);
        this.role = Objects.requireNonNull(role);
        if (versionCompleted < 0) throw new IllegalArgumentException("versionCompleted must not be negative");
        this.versionCompleted = versionCompleted;
        this.completedAt = completedAt;
    }

    public UserOnboardingState complete(int version, Instant at) {
        if (version < 1) throw new IllegalArgumentException("version must be positive");
        if (version <= versionCompleted) return this;
        return new UserOnboardingState(id, userId, role, version, Objects.requireNonNull(at));
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public Role getRole() { return role; }
    public int getVersionCompleted() { return versionCompleted; }
    public Instant getCompletedAt() { return completedAt; }
}
