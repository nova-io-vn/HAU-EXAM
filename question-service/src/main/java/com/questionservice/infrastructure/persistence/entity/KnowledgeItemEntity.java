package com.questionservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "knowledge_items")
public class KnowledgeItemEntity {
    @Id public UUID id;
    @Column(name = "topic_id", nullable = false) public UUID topicId;
    @Column(nullable = false, length = 100) public String code;
    @Column(nullable = false) public String name;
    @Column(nullable = false) public int ordinal;
    @Column(name = "target_easy", nullable = false) public int targetEasy;
    @Column(name = "target_medium", nullable = false) public int targetMedium;
    @Column(name = "target_hard", nullable = false) public int targetHard;
    @Column(name = "created_at", nullable = false) public Instant createdAt;
    @Column(name = "updated_at", nullable = false) public Instant updatedAt;
}
