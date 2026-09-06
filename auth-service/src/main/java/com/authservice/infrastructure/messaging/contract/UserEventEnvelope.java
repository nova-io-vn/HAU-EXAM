package com.authservice.infrastructure.messaging.contract;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserEventEnvelope(
        UUID eventId,
        String eventType,
        UUID correlationId,
        OffsetDateTime occurredAt,
        int version,
        UserSecuritySnapshotPayload payload
) { }
