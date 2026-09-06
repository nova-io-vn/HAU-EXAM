package com.questionservice.application.model;

import com.questionservice.domain.model.*;

import java.util.UUID;

public record QuestionCriteria(String facultyId, UUID subjectId, UUID chapterId, UUID topicId, Difficulty difficulty,
                               QuestionStatus status, QuestionSource source, UUID createdBy, String keyword, int page,
                               int size, String sort) {
}
