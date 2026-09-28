package com.questionservice.application.model;

import com.questionservice.domain.model.*;

import java.util.*;

public record QuestionInput(String facultyId, UUID subjectId, UUID chapterId, UUID topicId, UUID knowledgeItemId, UUID assignmentId, String content,
                            String imageUrl, String storageKey, QuestionType type, Difficulty difficulty,
                            List<QuestionOption> options) {
    public QuestionInput(String facultyId, UUID subjectId, UUID chapterId, UUID topicId, String content,
                         String imageUrl, String storageKey, QuestionType type, Difficulty difficulty,
                         List<QuestionOption> options) {
        this(facultyId, subjectId, chapterId, topicId, null, null, content, imageUrl, storageKey, type, difficulty, options);
    }
}
