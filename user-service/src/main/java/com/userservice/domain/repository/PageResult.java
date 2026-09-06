package com.userservice.domain.repository;

import java.util.List;
public record PageResult<T>(List<T> content, int page, int size, long totalElements, int totalPages) { }
