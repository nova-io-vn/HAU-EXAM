package com.notificationservice.infrastructure.persistence.entity;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="telegram_connections") public class TelegramConnectionEntity {
 @Id @Column(name="user_id") private UUID userId; @Column(name="chat_id",nullable=false,unique=true,length=80) private String chatId;
 @Column(length=120) private String username; @Column(nullable=false) private boolean enabled; @Column(name="linked_at",nullable=false) private Instant linkedAt;
 public UUID getUserId(){return userId;} public void setUserId(UUID v){userId=v;} public String getChatId(){return chatId;} public void setChatId(String v){chatId=v;}
 public String getUsername(){return username;} public void setUsername(String v){username=v;} public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;} public Instant getLinkedAt(){return linkedAt;} public void setLinkedAt(Instant v){linkedAt=v;}
}
