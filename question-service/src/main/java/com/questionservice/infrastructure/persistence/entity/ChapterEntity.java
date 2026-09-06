package com.questionservice.infrastructure.persistence.entity;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="chapters") public class ChapterEntity { @Id public UUID id; @Column(name="subject_id",nullable=false) public UUID subjectId; @Column(nullable=false) public String code; @Column(nullable=false) public String name; @Column(name="ordinal_number",nullable=false) public int ordinal; @Column(name="created_at",nullable=false) public Instant createdAt; @Column(name="updated_at",nullable=false) public Instant updatedAt; }
