package com.authservice.application.port.out;

import java.util.Map;
import java.util.UUID;

public interface AuthEventPublisher {
    void publish(String eventType, String routingKey, UUID correlationId, Map<String, Object> payload);
}
