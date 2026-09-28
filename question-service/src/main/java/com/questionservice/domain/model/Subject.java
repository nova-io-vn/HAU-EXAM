package com.questionservice.domain.model;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public record Subject(UUID id, String managingFacultyId, Set<String> participatingFacultyIds, String code, String name,
                      Instant createdAt, Instant updatedAt) {
    public Subject {
        LinkedHashSet<String> scopes = new LinkedHashSet<>();
        if (managingFacultyId != null && !managingFacultyId.isBlank()) scopes.add(managingFacultyId.trim());
        if (participatingFacultyIds != null) {
            participatingFacultyIds.stream()
                    .filter(value -> value != null && !value.isBlank())
                    .map(String::trim)
                    .forEach(scopes::add);
        }
        participatingFacultyIds = Set.copyOf(scopes);
    }

    public boolean isAvailableTo(String facultyId) {
        return facultyId != null && participatingFacultyIds.contains(facultyId);
    }
}
