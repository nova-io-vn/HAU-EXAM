package com.userservice.domain.repository;

public record PageQuery(int page, int size) {
    public PageQuery {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("Invalid page request");
    }
}
