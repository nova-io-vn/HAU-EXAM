package com.examservice.domain.model;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record Exam(UUID id, String name, String examCode, int durationMinutes, ExamStatus status,
                   String facultyId, UUID subjectId, UUID matrixId, UUID templateId, UUID createdBy,
                   int versionStartCode, int versionEndCode, boolean shuffleQuestions, boolean shuffleAnswers,
                   boolean allowQuestionReplacement, boolean reuseWarning, List<ExamVersion> versions,
                   Instant createdAt, Instant updatedAt) {
    public Exam {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Exam name is required");
        if (examCode == null || examCode.isBlank()) throw new IllegalArgumentException("Exam code is required");
        if (durationMinutes < 1 || durationMinutes > 600)
            throw new IllegalArgumentException("Exam duration must be between 1 and 600 minutes");
        if (status == null) throw new IllegalArgumentException("Exam status is required");
        if (versionStartCode < 1 || versionEndCode < versionStartCode)
            throw new IllegalArgumentException("Invalid exam version range");
        versions = List.copyOf(versions);
    }

    public Exam(UUID id, String name, String examCode, int durationMinutes, ExamStatus status,
                String facultyId, UUID subjectId, UUID matrixId, UUID templateId, UUID createdBy,
                List<ExamVersion> versions, Instant createdAt, Instant updatedAt) {
        this(id, name, examCode, durationMinutes, status, facultyId, subjectId, matrixId, templateId, createdBy,
                versions.stream().mapToInt(ExamVersion::versionCode).min().orElse(1),
                versions.stream().mapToInt(ExamVersion::versionCode).max().orElse(1),
                false, false, false, false, versions, createdAt, updatedAt);
    }

    public long totalQuestionSelections() {
        return versions.stream().mapToLong(version -> version.questions().size()).sum();
    }

    public long uniqueQuestionCount() {
        Set<UUID> ids = new HashSet<>();
        versions.forEach(version -> version.questions().forEach(question -> ids.add(question.questionId())));
        return ids.size();
    }

    public long reuseCount() {
        return totalQuestionSelections() - uniqueQuestionCount();
    }

    public double reuseRate() {
        return totalQuestionSelections() == 0 ? 0 : Math.round(reuseCount() * 1000d / totalQuestionSelections()) / 10d;
    }
}
