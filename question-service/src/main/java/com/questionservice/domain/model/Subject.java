package com.questionservice.domain.model;
import java.time.Instant;
import java.util.UUID;
public record Subject(UUID id, String facultyId, String code, String name, Instant createdAt, Instant updatedAt) { }
