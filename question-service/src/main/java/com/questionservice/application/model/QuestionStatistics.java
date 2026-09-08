package com.questionservice.application.model;

public record QuestionStatistics(long total, long draft, long pendingReview, long approved, long needRevision,
                                 long rejected) {
}
