package com.examservice.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ExamVersion(UUID id, int versionCode, long generationSeed, UUID createdBy,
                          List<ExamQuestionReference> questions, Instant createdAt) {
    public ExamVersion {
        if (versionCode < 1) throw new IllegalArgumentException("Version code must be positive");
        questions = List.copyOf(questions);
        if (questions.stream().map(ExamQuestionReference::questionId).distinct().count() != questions.size())
            throw new IllegalArgumentException("Duplicate question in exam version");
        if (questions.stream().map(ExamQuestionReference::position).distinct().count() != questions.size())
            throw new IllegalArgumentException("Duplicate question position in exam version");
    }

    public int versionNumber() {
        return versionCode;
    }

    public int questionCount() {
        return questions.size();
    }

    public ExamVersion(UUID id, int versionCode, List<ExamQuestionReference> questions, Instant createdAt) {
        this(id, versionCode, 0, null, questions, createdAt);
    }
}
