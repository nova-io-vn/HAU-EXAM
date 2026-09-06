package com.authservice.infrastructure.persistence.repository;

import com.authservice.infrastructure.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface JpaRefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {
    List<RefreshTokenEntity> findByAccountIdAndRevokedAtIsNull(UUID accountId);
    long deleteByExpiresAtBefore(Instant now);
}
