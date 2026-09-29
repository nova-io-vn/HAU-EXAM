package com.examservice.presentation.request;

import com.examservice.domain.model.Difficulty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;

public final class ExamRequests {
    private ExamRequests() { }

    public record MatrixRequest(@NotBlank String name, @NotBlank String facultyId, @NotNull UUID subjectId,
                                @Min(1) int totalQuestions, @NotEmpty @Valid List<RuleRequest> rules) { }
    public record RuleRequest(@NotNull UUID chapterId, UUID topicId, UUID knowledgeItemId,
                              @NotNull Difficulty difficulty, @Min(0) int questionCount) { }
    public record TemplateRequest(@NotBlank String name, @NotNull UUID matrixId, String header,
                                  String instructions) { }
    public record GenerateRequest(@NotBlank String name, @NotBlank @Size(max = 50) String examCode,
                                  @Min(1) @Max(600) int durationMinutes, @NotNull UUID matrixId,
                                  UUID templateId, @Min(1) Integer startCode, @Min(1) Integer endCode,
                                  boolean shuffleQuestions, boolean shuffleAnswers,
                                  boolean allowQuestionReplacement) {
        public int resolvedStartCode() { return startCode == null ? 1 : startCode; }
        public int resolvedEndCode() { return endCode == null ? resolvedStartCode() : endCode; }
    }
}
