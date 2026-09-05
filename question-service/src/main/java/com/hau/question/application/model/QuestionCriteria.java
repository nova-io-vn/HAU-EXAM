package com.hau.question.application.model;
import com.hau.question.domain.model.*;
import java.util.UUID;
public record QuestionCriteria(String facultyId, UUID subjectId, UUID chapterId, UUID topicId, Difficulty difficulty,
 QuestionStatus status, QuestionSource source, UUID createdBy, String keyword, int page, int size, String sort) { }
