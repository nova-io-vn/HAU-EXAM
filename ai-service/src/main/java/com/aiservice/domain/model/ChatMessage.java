package com.aiservice.domain.model;
import java.time.Instant;
import java.util.UUID;
public record ChatMessage(UUID id, UUID conversationId, ChatRole role, String content, String status, Instant createdAt) {}
