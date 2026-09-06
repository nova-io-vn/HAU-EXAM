package com.authservice.infrastructure.messaging;

import com.authservice.application.port.out.AuthEventPublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class RabbitAuthEventPublisher implements AuthEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    public RabbitAuthEventPublisher(RabbitTemplate rabbitTemplate) { this.rabbitTemplate = rabbitTemplate; }
    public void publish(String eventType, String routingKey, UUID correlationId, Map<String, Object> payload) {
        Map<String, Object> event = new HashMap<>(); event.put("eventId", UUID.randomUUID()); event.put("eventType", eventType);
        event.put("correlationId", correlationId == null ? UUID.randomUUID() : correlationId);
        event.put("occurredAt", Instant.now()); event.put("version", 1); event.put("payload", payload);
        rabbitTemplate.convertAndSend("auth.exchange", routingKey, event);
    }
}
