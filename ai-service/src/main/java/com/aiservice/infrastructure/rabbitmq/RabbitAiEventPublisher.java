package com.aiservice.infrastructure.rabbitmq;

import com.aiservice.application.port.out.AiEventPublisher;
import com.aiservice.domain.model.AiJob;

import java.time.*;
import java.util.*;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitAiEventPublisher implements AiEventPublisher {
    private final RabbitTemplate rabbit;

    public RabbitAiEventPublisher(RabbitTemplate r) {
        rabbit = r;
    }

    public void requested(AiJob j, UUID c) {
        send("ai.generation.requested", "AI_GENERATION_REQUESTED", j, c, Map.of("jobId", j.id(), "jobType", j.type()));
    }

    public void completed(AiJob j, UUID c) {
        var p = new LinkedHashMap<String, Object>();
        p.put("jobId", j.id());
        p.put("requestedBy", j.requestedBy());
        p.put("facultyId", j.facultyId());
        p.put("subjectId", j.subjectId());
        p.put("chapterId", j.chapterId());
        p.put("topicId", j.topicId());
        p.put("resultReference", j.resultReference());
        send("ai.generation.completed", "AI_GENERATION_COMPLETED", j, c, p);
    }

    public void failed(AiJob j, UUID c) {
        send("ai.generation.failed", "AI_GENERATION_FAILED", j, c, Map.of("jobId", j.id(), "errorCode", j.errorCode(), "requestedBy", j.requestedBy()));
    }

    private void send(String key, String type, AiJob j, UUID c, Object payload) {
        rabbit.convertAndSend(RabbitConfiguration.EXCHANGE, key, new Outbound(UUID.randomUUID(), type, c == null ? UUID.randomUUID() : c, OffsetDateTime.now(ZoneOffset.UTC), 1, payload));
    }

    record Outbound(UUID eventId, String eventType, UUID correlationId, OffsetDateTime occurredAt, int version,
                    Object payload) {
    }
}
