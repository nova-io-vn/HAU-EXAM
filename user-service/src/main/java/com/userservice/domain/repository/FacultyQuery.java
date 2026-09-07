package com.userservice.domain.repository;

public record FacultyQuery(String keyword, Boolean active, int page, int size) {
    public FacultyQuery { if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("Invalid page request"); }
}
