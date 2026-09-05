package com.hau.question.application.port.out;
import com.hau.question.domain.model.Question; import java.util.UUID;
public interface QuestionEventPublisher { void publish(String routingKey, String eventType, Question question, UUID correlationId); }
