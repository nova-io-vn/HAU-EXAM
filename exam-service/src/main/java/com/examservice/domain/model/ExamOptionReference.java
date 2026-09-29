package com.examservice.domain.model;

import java.util.Objects;
import java.util.UUID;

public record ExamOptionReference(UUID id, UUID optionId, int displayOrder) {
    public ExamOptionReference {
        Objects.requireNonNull(id);
        Objects.requireNonNull(optionId);
        if (displayOrder < 1) throw new IllegalArgumentException("Option display order must be positive");
    }
}
