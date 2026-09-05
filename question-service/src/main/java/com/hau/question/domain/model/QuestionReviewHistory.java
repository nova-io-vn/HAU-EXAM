package com.hau.question.domain.model;

import java.time.Instant;
import java.util.UUID;

public record QuestionReviewHistory(UUID id, UUID reviewerId, ReviewAction action, String comment, Instant createdAt) { }
