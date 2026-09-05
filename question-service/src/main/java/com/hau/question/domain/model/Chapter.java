package com.hau.question.domain.model;
import java.time.Instant;
import java.util.UUID;
public record Chapter(UUID id, UUID subjectId, String code, String name, int ordinal, Instant createdAt, Instant updatedAt) { }
