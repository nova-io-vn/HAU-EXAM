package com.questionservice.domain.model;

import java.time.Instant;
import java.util.UUID;

public record QuestionReviewHistory(UUID id, UUID reviewerId, ReviewAction action,
                                    QuestionStatus fromStatus, QuestionStatus toStatus,
                                    String comment, Instant createdAt) {
    public QuestionReviewHistory(UUID id, UUID reviewerId, ReviewAction action, String comment, Instant createdAt) {
        this(id, reviewerId, action, null, null, comment, createdAt);
    }
}
