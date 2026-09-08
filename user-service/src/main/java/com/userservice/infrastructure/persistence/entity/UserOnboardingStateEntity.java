package com.userservice.infrastructure.persistence.entity;

import com.userservice.domain.model.Role;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_onboarding_states", uniqueConstraints = @UniqueConstraint(name = "uk_user_onboarding_role", columnNames = {"user_id", "role"}))
public class UserOnboardingStateEntity {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private Role role;
    @Column(name = "version_completed", nullable = false) private int versionCompleted;
    @Column(name = "completed_at") private Instant completedAt;

    protected UserOnboardingStateEntity() { }
    public UserOnboardingStateEntity(UUID id, UUID userId, Role role, int versionCompleted, Instant completedAt) {
        this.id = id; this.userId = userId; this.role = role; this.versionCompleted = versionCompleted; this.completedAt = completedAt;
    }
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public Role getRole() { return role; }
    public int getVersionCompleted() { return versionCompleted; }
    public void setVersionCompleted(int value) { versionCompleted = value; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant value) { completedAt = value; }
}
