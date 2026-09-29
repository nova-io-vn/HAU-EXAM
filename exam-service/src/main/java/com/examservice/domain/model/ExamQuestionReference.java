package com.examservice.domain.model;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record ExamQuestionReference(UUID id, UUID questionId, int position, UUID matrixRuleId,
                                    List<ExamOptionReference> options) {
    public ExamQuestionReference {
        Objects.requireNonNull(id);
        Objects.requireNonNull(questionId);
        Objects.requireNonNull(matrixRuleId);
        if (position < 1) throw new IllegalArgumentException("Question position must be positive");
        options = List.copyOf(options == null ? List.of() : options);
        if (options.stream().map(ExamOptionReference::optionId).distinct().count() != options.size())
            throw new IllegalArgumentException("Duplicate option in exam question");
        if (options.stream().map(ExamOptionReference::displayOrder).distinct().count() != options.size())
            throw new IllegalArgumentException("Duplicate option display order");
    }

    public ExamQuestionReference(UUID id, UUID questionId, int position, UUID matrixRuleId) {
        this(id, questionId, position, matrixRuleId, List.of());
    }
}
