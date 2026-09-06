package com.aiservice.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_events")
public class ProcessedEventEntity {
    @Id
    public UUID eventId;
    @Column(name = "event_type", nullable = false)
    public String eventType;
    @Column(name = "processed_at", nullable = false)
    public Instant processedAt;
}
