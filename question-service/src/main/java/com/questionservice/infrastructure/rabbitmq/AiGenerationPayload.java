package com.questionservice.infrastructure.rabbitmq;
import java.util.UUID;
public record AiGenerationPayload(UUID jobId, UUID requestedBy, String facultyId, UUID subjectId, UUID chapterId, UUID topicId, String resultReference) {}
