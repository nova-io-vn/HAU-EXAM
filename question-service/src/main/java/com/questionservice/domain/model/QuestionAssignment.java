package com.questionservice.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record QuestionAssignment(UUID id, String facultyId, UUID subjectId, UUID chapterId, UUID topicId,
                                 UUID knowledgeItemId, UUID lecturerId, UUID assignedBy,
                                 int requiredQuestionCount, int requiredEasy, int requiredMedium, int requiredHard,
                                 LocalDate deadline, String note, Instant createdAt, Instant updatedAt) {
    public QuestionAssignment {
        if (id == null || facultyId == null || facultyId.isBlank() || subjectId == null || lecturerId == null || assignedBy == null || deadline == null)
            throw new IllegalArgumentException("Assignment identity, scope, lecturer and deadline are required");
        if (requiredQuestionCount <= 0 || requiredEasy < 0 || requiredMedium < 0 || requiredHard < 0
                || requiredEasy + requiredMedium + requiredHard != requiredQuestionCount)
            throw new IllegalArgumentException("Difficulty requirements must be non-negative and equal requiredQuestionCount");
        if (note != null && note.length() > 1000) throw new IllegalArgumentException("Assignment note is too long");
    }
}
