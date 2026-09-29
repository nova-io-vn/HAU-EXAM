package com.examservice.application.port.out;

import com.examservice.domain.model.Difficulty;
import java.util.List;
import java.util.UUID;

public interface QuestionCatalogPort {
    List<QuestionCandidate> approvedQuestions(String facultyId, UUID subjectId, UUID chapterId, UUID topicId,
                                              UUID knowledgeItemId, Difficulty difficulty, String bearerToken);

    default QuestionDetails question(UUID id, String bearerToken) {
        throw new UnsupportedOperationException("Question details are not available");
    }

    record QuestionCandidate(UUID id, String facultyId, UUID subjectId, UUID chapterId, UUID topicId,
                             UUID knowledgeItemId, Difficulty difficulty, String status) { }

    record QuestionDetails(UUID id, String type, String content, List<Option> options) {
        public QuestionDetails {
            options = List.copyOf(options == null ? List.of() : options);
        }

        public boolean answerShuffleAllowed() {
            return "SINGLE_CHOICE".equals(type) || "MULTIPLE_CHOICE".equals(type);
        }
    }

    record Option(UUID id, String label, String content, boolean correct, int sortOrder) { }
}
