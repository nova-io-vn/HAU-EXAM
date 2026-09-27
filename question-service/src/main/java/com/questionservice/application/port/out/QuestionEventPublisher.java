package com.questionservice.application.port.out;

import com.questionservice.domain.model.Question;
import com.questionservice.domain.model.Actor;

import java.util.List;
import java.util.UUID;

public interface QuestionEventPublisher {
    void publish(String routingKey, String eventType, Question question, UUID correlationId);
    void publishBulk(String routingKey, String eventType, Actor actor, List<Question> questions, UUID correlationId);
}
