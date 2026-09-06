package com.userservice.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_events")
public class ProcessedEventEntity {
    @Id
    @Column(name = "event_id")
    private UUID eventId;
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;
    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    protected ProcessedEventEntity() {
    }

    public ProcessedEventEntity(UUID eventId, String eventType, Instant processedAt) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.processedAt = processedAt;
    }

    public UUID getEventId() {
        return eventId;
    }
}
