package com.userservice.presentation.response;

import com.userservice.domain.model.Faculty;

import java.time.Instant;
import java.util.UUID;

public record FacultyResponse(UUID id, String code, String name, String description, boolean active, Instant createdAt,
                              Instant updatedAt, long lecturerCount, java.util.List<SubjectAdmin> subjectAdmins) {
    public static FacultyResponse from(Faculty f) {
        return new FacultyResponse(f.id(), f.code(), f.name(), f.description(), f.active(), f.createdAt(), f.updatedAt(), 0, java.util.List.of());
    }
    public record SubjectAdmin(UUID id, String fullName, String lecturerCode, String avatarUrl, String facultyId) {}
}
