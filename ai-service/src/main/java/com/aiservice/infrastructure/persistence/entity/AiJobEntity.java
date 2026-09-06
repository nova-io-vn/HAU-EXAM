package com.aiservice.infrastructure.persistence.entity;

import com.aiservice.domain.model.*;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_jobs")
public class AiJobEntity {
    @Id
    public UUID id;
    @Column(name = "requested_by", nullable = false)
    public UUID requestedBy;
    @Column(name = "document_id")
    public UUID documentId;
    @Column(name = "faculty_id")
    public String facultyId;
    @Column(name = "subject_id")
    public UUID subjectId;
    @Column(name = "chapter_id")
    public UUID chapterId;
    @Column(name = "topic_id")
    public UUID topicId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public JobType type;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public JobStatus status;
    @Column(name = "request_json", columnDefinition = "text")
    public String requestJson;
    @Column(name = "result_reference")
    public String resultReference;
    @Column(name = "error_code")
    public String errorCode;
    @Column(name = "error_message")
    public String errorMessage;
    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
    @Column(name = "started_at")
    public Instant startedAt;
    @Column(name = "completed_at")
    public Instant completedAt;
    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;
    @Version
    public long version;
}
