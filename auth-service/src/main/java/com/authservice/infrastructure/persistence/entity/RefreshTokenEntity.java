package com.authservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenEntity {
    @Id private UUID id;
    @Column(name = "account_id", nullable = false) private UUID accountId;
    @Column(name = "token_hash", nullable = false, length = 100) private String tokenHash;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(name = "revoked_at") private Instant revokedAt;
    public RefreshTokenEntity() { }
    public UUID getId() { return id; } public void setId(UUID id) { this.id = id; }
    public UUID getAccountId() { return accountId; } public void setAccountId(UUID value) { accountId = value; }
    public String getTokenHash() { return tokenHash; } public void setTokenHash(String value) { tokenHash = value; }
    public Instant getExpiresAt() { return expiresAt; } public void setExpiresAt(Instant value) { expiresAt = value; }
    public Instant getRevokedAt() { return revokedAt; } public void setRevokedAt(Instant value) { revokedAt = value; }
}
