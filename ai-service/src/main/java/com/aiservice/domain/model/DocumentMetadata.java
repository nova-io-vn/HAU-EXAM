package com.aiservice.domain.model;

import java.time.Instant;
import java.util.UUID;

public record DocumentMetadata(UUID id, UUID ownerId, String originalName, String contentType, long size,
                               String storageKey, String checksum, Instant createdAt) {
    public DocumentMetadata {
        if (size <= 0) throw new IllegalArgumentException("Document is empty");
        if (storageKey == null || storageKey.isBlank()) throw new IllegalArgumentException("storageKey is required");
    }
}
