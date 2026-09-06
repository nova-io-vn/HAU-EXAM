package com.questionservice.domain.model;

import java.util.UUID;

public record QuestionOption(UUID id, String label, String content, String imageUrl,
                             String storageKey, boolean correct, int sortOrder) {
    public QuestionOption {
        if (label == null || label.isBlank() || content == null || content.isBlank())
            throw new IllegalArgumentException("Option label and content are required");
    }
}
