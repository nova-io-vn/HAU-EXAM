package com.questionservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "subject_lecturer_assignments", uniqueConstraints = @UniqueConstraint(columnNames = {"subject_id", "user_id"}))
public class SubjectLecturerAssignmentEntity {
    @Id public UUID id;
    @Column(name = "subject_id", nullable = false) public UUID subjectId;
    @Column(name = "user_id", nullable = false) public UUID userId;
    @Column(name = "assigned_by", nullable = false) public UUID assignedBy;
    @Column(name = "assigned_at", nullable = false) public Instant assignedAt;
    @Column(nullable = false) public boolean active;
}
