package com.examservice.domain.model;

import java.util.Objects;
import java.util.UUID;

public record ExamMatrixRule(UUID id, UUID chapterId, UUID topicId, UUID knowledgeItemId,
                             Difficulty difficulty, int questionCount) {
    public ExamMatrixRule {
        Objects.requireNonNull(chapterId);
        Objects.requireNonNull(difficulty);
        if (knowledgeItemId != null && topicId == null)
            throw new IllegalArgumentException("Knowledge item requires a topic");
        if (questionCount < 0) throw new IllegalArgumentException("Question count cannot be negative");
    }

    public ExamMatrixRule(UUID id, UUID chapterId, UUID topicId, Difficulty difficulty, int questionCount) {
        this(id, chapterId, topicId, null, difficulty, questionCount);
    }
}
