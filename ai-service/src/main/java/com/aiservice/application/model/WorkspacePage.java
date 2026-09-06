package com.aiservice.application.model;

import java.util.List;

public record WorkspacePage<T>(List<T> items, int page, int size, long totalElements, int totalPages) {}
