package com.aiservice.infrastructure.persistence.entity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="ai_chat_messages") public class ChatMessageEntity {
 @Id public UUID id; @Column(name="conversation_id",nullable=false) public UUID conversationId; @Enumerated(EnumType.STRING) @Column(nullable=false) public com.aiservice.domain.model.ChatRole role;
 @Column(nullable=false,columnDefinition="text") public String content; @Column(nullable=false) public String status; @Column(name="created_at",nullable=false) public Instant createdAt;
}
