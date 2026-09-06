package com.questionservice.application.port.out;
import com.questionservice.domain.model.Question; import java.util.UUID;
public interface QuestionEventPublisher { void publish(String routingKey, String eventType, Question question, UUID correlationId); }
