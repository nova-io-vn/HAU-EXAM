package com.hau.question.infrastructure.persistence.entity;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="subjects") public class SubjectEntity { @Id public UUID id; @Column(name="faculty_id",nullable=false) public String facultyId; @Column(nullable=false) public String code; @Column(nullable=false) public String name; @Column(name="created_at",nullable=false) public Instant createdAt; @Column(name="updated_at",nullable=false) public Instant updatedAt; }
