package com.notificationservice.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class DeviceToken {
    private final UUID id;
    private final UUID userId;
    private final String token;
    private final DevicePlatform platform;
    private final String deviceIdentifier;
    private final boolean active;
    private final Instant createdAt;
    private final Instant updatedAt;

    public DeviceToken(UUID id, UUID userId, String token, DevicePlatform platform, String deviceIdentifier,
                       boolean active, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId);
        this.token = require(token, 512);
        this.platform = Objects.requireNonNull(platform);
        this.deviceIdentifier = optional(deviceIdentifier, 255);
        this.active = active;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static DeviceToken register(UUID userId, String token, DevicePlatform platform, String deviceIdentifier, Instant now) {
        return new DeviceToken(UUID.randomUUID(), userId, token, platform, deviceIdentifier, true, now, now);
    }

    public DeviceToken activateFor(UUID nextUserId, DevicePlatform nextPlatform, String nextDeviceIdentifier, Instant now) {
        return new DeviceToken(id, nextUserId, token, nextPlatform, nextDeviceIdentifier, true, createdAt, now);
    }

    public DeviceToken deactivate(Instant now) {
        return new DeviceToken(id, userId, token, platform, deviceIdentifier, false, createdAt, now);
    }

    private static String require(String value, int max) {
        if (value == null || value.isBlank() || value.length() > max) throw new IllegalArgumentException("Invalid device token");
        return value;
    }
    private static String optional(String value, int max) { return value == null || value.isBlank() ? null : value.substring(0, Math.min(value.length(), max)); }
    public UUID id() { return id; } public UUID userId() { return userId; } public String token() { return token; }
    public DevicePlatform platform() { return platform; } public String deviceIdentifier() { return deviceIdentifier; }
    public boolean active() { return active; } public Instant createdAt() { return createdAt; } public Instant updatedAt() { return updatedAt; }
}
