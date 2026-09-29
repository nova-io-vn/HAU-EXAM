package com.notificationservice.infrastructure.persistence.repository;
import com.notificationservice.infrastructure.persistence.entity.TelegramLinkTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.Optional;

public interface JpaTelegramLinkTokenRepository extends JpaRepository<TelegramLinkTokenEntity, String> {
    Optional<TelegramLinkTokenEntity> findByTokenHashAndConsumedAtIsNull(String hash);

    @Modifying
    @Query("update TelegramLinkTokenEntity t set t.consumedAt = :consumedAt where t.tokenHash = :tokenHash and t.consumedAt is null and t.expiresAt > :now")
    int consumeIfAvailable(@Param("tokenHash") String tokenHash, @Param("now") Instant now, @Param("consumedAt") Instant consumedAt);
}
