package com.authservice.infrastructure.persistence.adapter;

import com.authservice.application.port.out.RefreshTokenStore;
import com.authservice.infrastructure.persistence.entity.RefreshTokenEntity;
import com.authservice.infrastructure.persistence.repository.JpaRefreshTokenRepository;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RefreshTokenPersistenceAdapter implements RefreshTokenStore {
    private final JpaRefreshTokenRepository repository;
    public RefreshTokenPersistenceAdapter(JpaRefreshTokenRepository repository) { this.repository = repository; }
    public void save(UUID tokenId, UUID accountId, String tokenHash, Instant expiresAt) {
        RefreshTokenEntity e = new RefreshTokenEntity(); e.setId(tokenId); e.setAccountId(accountId);
        e.setTokenHash(tokenHash); e.setExpiresAt(expiresAt); repository.save(e);
    }
    public Optional<StoredRefreshToken> find(UUID tokenId) {
        return repository.findById(tokenId).map(e -> new StoredRefreshToken(e.getId(), e.getAccountId(), e.getTokenHash(), e.getExpiresAt(), e.getRevokedAt()));
    }
    public void revoke(UUID tokenId, Instant revokedAt) { repository.findById(tokenId).ifPresent(e -> { e.setRevokedAt(revokedAt); repository.save(e); }); }
    public void revokeAllForAccount(UUID accountId, Instant revokedAt) { repository.findByAccountIdAndRevokedAtIsNull(accountId).forEach(e -> e.setRevokedAt(revokedAt)); }
}
