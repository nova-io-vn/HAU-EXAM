package com.questionservice.infrastructure.persistence.entity;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="topics") public class TopicEntity { @Id public UUID id; @Column(name="chapter_id",nullable=false) public UUID chapterId; @Column(nullable=false) public String code; @Column(nullable=false) public String name; @Column(name="created_at",nullable=false) public Instant createdAt; @Column(name="updated_at",nullable=false) public Instant updatedAt; }
