package com.aiservice.infrastructure.persistence.entity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="ai_chat_attachments") public class ChatAttachmentEntity {
 @Id public UUID id; @Column(name="conversation_id",nullable=false) public UUID conversationId; @Column(name="document_id",nullable=false) public UUID documentId; @Column(name="attached_at",nullable=false) public Instant attachedAt;
}
