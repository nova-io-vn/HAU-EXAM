package com.userservice.domain.repository;

import com.userservice.domain.model.Role;
import com.userservice.domain.model.UserStatus;

public record PageQuery(int page, int size, String keyword, String facultyId, Role role, UserStatus status, String sort) {
    public PageQuery(int page, int size) { this(page, size, null, null, null, null, null); }
    public PageQuery {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("Invalid page request");
    }
}
