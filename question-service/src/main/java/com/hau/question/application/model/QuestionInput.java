package com.hau.question.application.model;
import com.hau.question.domain.model.*; import java.util.*;
public record QuestionInput(String facultyId, UUID subjectId, UUID chapterId, UUID topicId, String content,
 String imageUrl, String storageKey, QuestionType type, Difficulty difficulty, List<QuestionOption> options) { }
