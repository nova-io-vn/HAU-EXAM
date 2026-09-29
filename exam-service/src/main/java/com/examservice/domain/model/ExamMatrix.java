package com.examservice.domain.model;

import com.examservice.domain.exception.InvalidMatrixException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record ExamMatrix(UUID id, String name, String facultyId, UUID subjectId, int totalQuestions,
                         List<ExamMatrixRule> rules, UUID createdBy, Instant createdAt, Instant updatedAt) {
    public ExamMatrix {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Matrix name is required");
        if (facultyId == null || facultyId.isBlank()) throw new IllegalArgumentException("facultyId is required");
        if (totalQuestions <= 0) throw new InvalidMatrixException("Total questions must be positive");
        rules = List.copyOf(rules);
        validate(totalQuestions, rules);
    }

    public static void validate(int total, List<ExamMatrixRule> rules) {
        if (rules == null || rules.isEmpty()) throw new InvalidMatrixException("Matrix requires distribution rules");
        int sum = rules.stream().mapToInt(ExamMatrixRule::questionCount).sum();
        if (sum != total) throw new InvalidMatrixException("Rule total " + sum + " does not match matrix total " + total);
        for (int first = 0; first < rules.size(); first++) {
            for (int second = first + 1; second < rules.size(); second++) {
                if (overlap(rules.get(first), rules.get(second))) {
                    throw new InvalidMatrixException("Overlapping distribution rules are not allowed");
                }
            }
        }
    }

    private static boolean overlap(ExamMatrixRule left, ExamMatrixRule right) {
        if (!left.chapterId().equals(right.chapterId())
                || !Objects.equals(left.topicId(), right.topicId())
                || left.difficulty() != right.difficulty()) return false;
        return left.knowledgeItemId() == null || right.knowledgeItemId() == null
                || left.knowledgeItemId().equals(right.knowledgeItemId());
    }
}
