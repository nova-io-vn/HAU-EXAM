package com.aiservice.presentation.request;

import jakarta.validation.constraints.*;

import java.util.UUID;

public final class AiRequests {
    private AiRequests() {
    }

    public enum OutputLanguage { VI, EN }

    public record GenerateRequest(@NotNull UUID documentId, @Min(1) @Max(100) int count, String difficulty,
                                  UUID topicId, UUID subjectId, UUID chapterId, OutputLanguage language,
                                  Boolean includeImages) {
        public OutputLanguage languageOrDefault() { return language == null ? OutputLanguage.VI : language; }
        public boolean includeImagesOrDefault() { return Boolean.TRUE.equals(includeImages); }
    }

    public record AnalyzeRequest(@NotNull UUID documentId, @NotBlank String analysisType) {
    }

    public record ChatRequest(UUID documentId, @NotBlank @Size(max = 4000) String message) {
    }
}
