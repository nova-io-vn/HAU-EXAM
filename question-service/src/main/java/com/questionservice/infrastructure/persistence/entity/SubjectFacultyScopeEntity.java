package com.questionservice.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

@Entity
@Table(name = "subject_faculty_scopes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"subject_id", "faculty_id"}))
public class SubjectFacultyScopeEntity {
    @Id
    public UUID id;
    @Column(name = "subject_id", nullable = false)
    public UUID subjectId;
    @Column(name = "faculty_id", nullable = false)
    public String facultyId;
    @Column(nullable = false)
    public boolean active;
}
