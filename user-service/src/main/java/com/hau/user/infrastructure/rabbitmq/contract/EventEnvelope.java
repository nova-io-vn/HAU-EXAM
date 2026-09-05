package com.hau.user.infrastructure.rabbitmq.contract;

import java.time.OffsetDateTime;
import java.util.UUID;
public record EventEnvelope<T>(UUID eventId,String eventType,UUID correlationId,OffsetDateTime occurredAt,int version,T payload) { }
