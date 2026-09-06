package com.aiservice.domain.model;

import com.aiservice.domain.exception.InvalidJobTransitionException;

import java.time.Instant;
import java.util.*;

public final class AiJob {
    private final UUID id, requestedBy, documentId, subjectId, chapterId, topicId;
    private final String facultyId;
    private final JobType type;
    private JobStatus status;
    private final String requestJson;
    private String resultReference, errorCode, errorMessage;
    private final Instant createdAt;
    private Instant startedAt, completedAt, updatedAt;

    public AiJob(UUID id, UUID user, UUID doc, JobType type, JobStatus status, String request, String result, String code, String error, Instant created, Instant started, Instant completed, Instant updated) {
        this(id, user, doc, null, null, null, null, type, status, request, result, code, error, created, started, completed, updated);
    }

    public AiJob(UUID id, UUID user, UUID doc, String faculty, UUID subject, UUID chapter, UUID topic, JobType type, JobStatus status, String request, String result, String code, String error, Instant created, Instant started, Instant completed, Instant updated) {
        this.id = Objects.requireNonNull(id);
        requestedBy = Objects.requireNonNull(user);
        documentId = doc;
        facultyId = faculty;
        subjectId = subject;
        chapterId = chapter;
        topicId = topic;
        this.type = Objects.requireNonNull(type);
        this.status = Objects.requireNonNull(status);
        requestJson = request;
        resultReference = result;
        errorCode = code;
        errorMessage = error;
        createdAt = created;
        startedAt = started;
        completedAt = completed;
        updatedAt = updated;
    }

    public static AiJob pending(UUID id, UUID user, UUID doc, JobType type, String request, Instant now) {
        return pending(id, user, doc, null, null, null, null, type, request, now);
    }

    public static AiJob pending(UUID id, UUID user, UUID doc, String faculty, UUID subject, UUID chapter, UUID topic, JobType type, String request, Instant now) {
        return new AiJob(id, user, doc, faculty, subject, chapter, topic, type, JobStatus.PENDING, request, null, null, null, now, null, null, now);
    }

    public void start(Instant now) {
        require(JobStatus.PENDING);
        status = JobStatus.PROCESSING;
        startedAt = now;
        updatedAt = now;
    }

    public void complete(String ref, Instant now) {
        require(JobStatus.PROCESSING);
        if (ref == null || ref.isBlank()) throw new IllegalArgumentException("result reference required");
        status = JobStatus.COMPLETED;
        resultReference = ref;
        completedAt = now;
        updatedAt = now;
    }

    public void fail(String code, String message, Instant now) {
        if (status != JobStatus.PROCESSING && status != JobStatus.PENDING)
            throw new InvalidJobTransitionException("Cannot fail job in " + status);
        status = JobStatus.FAILED;
        errorCode = code;
        errorMessage = message;
        completedAt = now;
        updatedAt = now;
    }

    private void require(JobStatus s) {
        if (status != s) throw new InvalidJobTransitionException("Expected " + s + " but was " + status);
    }

    public UUID id() {
        return id;
    }

    public UUID requestedBy() {
        return requestedBy;
    }

    public UUID documentId() {
        return documentId;
    }

    public String facultyId() {
        return facultyId;
    }

    public UUID subjectId() {
        return subjectId;
    }

    public UUID chapterId() {
        return chapterId;
    }

    public UUID topicId() {
        return topicId;
    }

    public JobType type() {
        return type;
    }

    public JobStatus status() {
        return status;
    }

    public String requestJson() {
        return requestJson;
    }

    public String resultReference() {
        return resultReference;
    }

    public String errorCode() {
        return errorCode;
    }

    public String errorMessage() {
        return errorMessage;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant startedAt() {
        return startedAt;
    }

    public Instant completedAt() {
        return completedAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
