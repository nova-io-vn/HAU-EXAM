package com.aiservice.infrastructure.rabbitmq;

import com.aiservice.application.service.AiJobProcessor;
import com.aiservice.domain.exception.ProviderException;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AiJobConsumerTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void malformedPayloadGoesStraightToDlqWithoutProcessing() throws Exception {
        AiJobProcessor processor = mock(AiJobProcessor.class);
        RabbitTemplate rabbit = mock(RabbitTemplate.class);
        Channel channel = mock(Channel.class);
        Message message = message();
        var event = new EventEnvelope(UUID.randomUUID(), "AI_GENERATION_REQUESTED", UUID.randomUUID(),
                OffsetDateTime.now(), 1, mapper.createObjectNode().put("jobId", "not-a-uuid"));

        new AiJobConsumer(processor, rabbit, 3).consume(event, message, channel);

        verifyNoInteractions(processor);
        verify(rabbit).send("ai.dlx", RabbitConfiguration.DLQ, message);
        verify(channel).basicAck(7L, false);
    }

    @Test
    void retryableProviderFailureUsesFiniteRetryQueue() throws Exception {
        AiJobProcessor processor = mock(AiJobProcessor.class);
        RabbitTemplate rabbit = mock(RabbitTemplate.class);
        Channel channel = mock(Channel.class);
        Message message = message();
        UUID eventId = UUID.randomUUID(), jobId = UUID.randomUUID(), correlation = UUID.randomUUID();
        doThrow(new ProviderException("timeout", true, null)).when(processor).process(eventId, jobId, correlation);

        new AiJobConsumer(processor, rabbit, 3).consume(event(eventId, jobId, correlation), message, channel);

        verify(rabbit).send("ai.retry.exchange", RabbitConfiguration.RETRY, message);
        verify(channel).basicAck(7L, false);
    }

    @Test
    void exhaustedRuntimeFailureMarksJobFailedAndDeadLetters() throws Exception {
        AiJobProcessor processor = mock(AiJobProcessor.class);
        RabbitTemplate rabbit = mock(RabbitTemplate.class);
        Channel channel = mock(Channel.class);
        Message message = message();
        message.getMessageProperties().setHeader("x-retry-count", 2);
        UUID eventId = UUID.randomUUID(), jobId = UUID.randomUUID(), correlation = UUID.randomUUID();
        doThrow(new IllegalStateException("database unavailable")).when(processor).process(eventId, jobId, correlation);

        new AiJobConsumer(processor, rabbit, 3).consume(event(eventId, jobId, correlation), message, channel);

        verify(processor).failAfterRetries(eq(eventId), eq(jobId), eq(correlation), any());
        verify(rabbit).send("ai.dlx", RabbitConfiguration.DLQ, message);
        verify(channel).basicAck(7L, false);
    }

    private EventEnvelope event(UUID eventId, UUID jobId, UUID correlation) {
        return new EventEnvelope(eventId, "AI_GENERATION_REQUESTED", correlation, OffsetDateTime.now(), 1,
                mapper.createObjectNode().put("jobId", jobId.toString()));
    }

    private Message message() {
        MessageProperties properties = new MessageProperties();
        properties.setDeliveryTag(7L);
        return new Message(new byte[0], properties);
    }
}
