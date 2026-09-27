package com.notificationservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "telegram_bot_settings")
public class TelegramBotSettingsEntity {
    @Id private UUID id;
    @Column(name="bot_username", nullable=false, length=120) private String botUsername;
    @Column(name="bot_token_encrypted", columnDefinition="text") private String botTokenEncrypted;
    @Column(nullable=false) private boolean enabled;
    @Column(name="updated_at", nullable=false) private Instant updatedAt;
    public UUID getId(){return id;} public void setId(UUID v){id=v;}
    public String getBotUsername(){return botUsername;} public void setBotUsername(String v){botUsername=v;}
    public String getBotTokenEncrypted(){return botTokenEncrypted;} public void setBotTokenEncrypted(String v){botTokenEncrypted=v;}
    public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
    public Instant getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Instant v){updatedAt=v;}
}
