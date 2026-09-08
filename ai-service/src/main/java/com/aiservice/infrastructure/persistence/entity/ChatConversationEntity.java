package com.aiservice.infrastructure.persistence.entity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="ai_chat_conversations") public class ChatConversationEntity {
 @Id public UUID id; @Column(name="user_id",nullable=false) public UUID userId; @Column(nullable=false) public String title;
 @Column(name="created_at",nullable=false) public Instant createdAt; @Column(name="updated_at",nullable=false) public Instant updatedAt;
}
