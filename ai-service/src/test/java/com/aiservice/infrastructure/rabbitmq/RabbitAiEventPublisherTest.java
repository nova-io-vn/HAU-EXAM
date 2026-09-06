package com.aiservice.infrastructure.rabbitmq;

import com.aiservice.domain.model.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.time.Instant;
import java.util.UUID;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RabbitAiEventPublisherTest {
    @Test void completedEventCarriesReferenceAndContextOnly() throws Exception {
        RabbitTemplate rabbit = mock(RabbitTemplate.class);
        RabbitAiEventPublisher publisher = new RabbitAiEventPublisher(rabbit);
        UUID jobId = UUID.randomUUID(), user = UUID.randomUUID(), subject = UUID.randomUUID(), chapter = UUID.randomUUID();
        AiJob job = AiJob.pending(jobId, user, UUID.randomUUID(), "CNTT", subject, chapter, null,
                JobType.QUESTION_GENERATION, "{}", Instant.now());
        job.start(Instant.now());
        job.complete("db:ai-results:" + jobId, Instant.now());
        publisher.completed(job, UUID.randomUUID());
        ArgumentCaptor<Object> event = ArgumentCaptor.forClass(Object.class);
        verify(rabbit).convertAndSend(eq(RabbitConfiguration.EXCHANGE), eq("ai.generation.completed"), event.capture());
        var payloadField = event.getValue().getClass().getDeclaredField("payload");
        payloadField.setAccessible(true);
        Object payload = payloadField.get(event.getValue());
        assertThat(payload).isInstanceOf(Map.class);
        Map<?, ?> payloadMap = (Map<?, ?>) payload;
        assertTrue(payloadMap.containsKey("jobId") && payloadMap.containsKey("requestedBy")
                && payloadMap.containsKey("facultyId") && payloadMap.containsKey("subjectId")
                && payloadMap.containsKey("chapterId") && payloadMap.containsKey("topicId")
                && payloadMap.containsKey("resultReference"));
        assertFalse(payloadMap.containsKey("questions"));
    }
}
