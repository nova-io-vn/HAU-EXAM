package com.hau.question.infrastructure.rabbitmq;
import tools.jackson.databind.JsonNode; import java.time.OffsetDateTime; import java.util.UUID;
public record EventEnvelope(UUID eventId,String eventType,UUID correlationId,OffsetDateTime occurredAt,int version,JsonNode payload){}
