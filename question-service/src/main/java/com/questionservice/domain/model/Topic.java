package com.questionservice.domain.model;
import java.time.Instant;
import java.util.UUID;
public record Topic(UUID id, UUID chapterId, String code, String name, Instant createdAt, Instant updatedAt) { }
