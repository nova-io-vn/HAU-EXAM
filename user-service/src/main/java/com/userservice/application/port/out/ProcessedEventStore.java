package com.userservice.application.port.out;

import java.time.Instant;
import java.util.UUID;
public interface ProcessedEventStore {
    boolean exists(UUID eventId);
    void record(UUID eventId, String eventType, Instant processedAt);
}
