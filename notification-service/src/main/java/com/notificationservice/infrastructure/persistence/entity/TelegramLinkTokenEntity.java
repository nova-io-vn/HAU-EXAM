package com.notificationservice.infrastructure.persistence.entity;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="telegram_link_tokens") public class TelegramLinkTokenEntity {
 @Id @Column(name="token_hash",length=128) private String tokenHash; @Column(name="user_id",nullable=false) private UUID userId; @Column(name="expires_at",nullable=false) private Instant expiresAt; @Column(name="consumed_at") private Instant consumedAt;
 public String getTokenHash(){return tokenHash;} public void setTokenHash(String v){tokenHash=v;} public UUID getUserId(){return userId;} public void setUserId(UUID v){userId=v;} public Instant getExpiresAt(){return expiresAt;} public void setExpiresAt(Instant v){expiresAt=v;} public Instant getConsumedAt(){return consumedAt;} public void setConsumedAt(Instant v){consumedAt=v;}
}
