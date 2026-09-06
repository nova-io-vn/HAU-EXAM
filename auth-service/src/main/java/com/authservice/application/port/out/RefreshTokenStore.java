package com.authservice.application.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenStore {
    void save(UUID tokenId, UUID accountId, String tokenHash, Instant expiresAt);
    Optional<StoredRefreshToken> find(UUID tokenId);
    void revoke(UUID tokenId, Instant revokedAt);
    void revokeAllForAccount(UUID accountId, Instant revokedAt);

    record StoredRefreshToken(UUID tokenId, UUID accountId, String tokenHash,
                              Instant expiresAt, Instant revokedAt) {
        public boolean isUsable(Instant now) {
            return revokedAt == null && expiresAt.isAfter(now);
        }
    }
}
