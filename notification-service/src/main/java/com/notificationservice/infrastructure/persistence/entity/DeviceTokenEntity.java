package com.notificationservice.infrastructure.persistence.entity;

import com.notificationservice.domain.model.DevicePlatform;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "device_tokens", uniqueConstraints = @UniqueConstraint(name = "uq_device_tokens_token", columnNames = "token"))
public class DeviceTokenEntity {
    @Id public UUID id;
    @Column(name = "user_id", nullable = false) public UUID userId;
    @Column(nullable = false, length = 512) public String token;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) public DevicePlatform platform;
    @Column(name = "device_identifier", length = 255) public String deviceIdentifier;
    @Column(nullable = false) public boolean active;
    @Column(name = "created_at", nullable = false) public Instant createdAt;
    @Column(name = "updated_at", nullable = false) public Instant updatedAt;
}
