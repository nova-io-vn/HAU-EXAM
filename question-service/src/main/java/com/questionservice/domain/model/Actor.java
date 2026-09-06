package com.questionservice.domain.model;
import java.util.UUID;
public record Actor(UUID userId, Role role, String facultyId) { }
