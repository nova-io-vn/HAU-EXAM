package com.aiservice.application.port.out;

import com.aiservice.domain.model.AiJob;
import java.util.UUID;

public interface AiEventPublisher {
    void requested(AiJob job, UUID correlationId);
    void completed(AiJob job, UUID correlationId);
    void failed(AiJob job, UUID correlationId);
}
