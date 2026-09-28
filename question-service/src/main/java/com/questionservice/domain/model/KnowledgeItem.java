package com.questionservice.domain.model;

import java.time.Instant;
import java.util.UUID;

public record KnowledgeItem(UUID id, UUID topicId, String code, String name, int ordinal,
                            int targetEasy, int targetMedium, int targetHard,
                            Instant createdAt, Instant updatedAt) {
    public KnowledgeItem {
        if (id == null || topicId == null) throw new IllegalArgumentException("Knowledge item identifiers are required");
        if (code == null || code.isBlank() || name == null || name.isBlank()) throw new IllegalArgumentException("Knowledge item code and name are required");
        if (ordinal < 0 || targetEasy < 0 || targetMedium < 0 || targetHard < 0) throw new IllegalArgumentException("Knowledge item order and targets must not be negative");
        code = code.trim();
        name = name.trim();
    }
}
