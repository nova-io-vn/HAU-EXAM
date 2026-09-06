package com.aiservice.infrastructure.rabbitmq;

import com.aiservice.application.service.AiJobProcessor;
import com.aiservice.domain.exception.ProviderException;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class AiJobConsumer {
    private static final Logger log = LoggerFactory.getLogger(AiJobConsumer.class);
    private static final String RETRY_COUNT = "x-retry-count";

    private final AiJobProcessor processor;
    private final RabbitTemplate rabbit;
    private final int maxRetries;

    public AiJobConsumer(AiJobProcessor processor, RabbitTemplate rabbit,
                         @Value("${ai.rabbitmq.max-retries}") int maxRetries) {
        this.processor = processor;
        this.rabbit = rabbit;
        this.maxRetries = maxRetries;
    }

    @RabbitListener(queues = RabbitConfiguration.QUEUE, concurrency = "${ai.rabbitmq.concurrency}")
    public void consume(EventEnvelope event, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        ParsedEvent parsed;
        try {
            parsed = parse(event);
        } catch (IllegalArgumentException exception) {
            log.warn("Malformed AI generation event sent to DLQ; eventId={}", event == null ? null : event.eventId());
            rabbit.send("ai.dlx", RabbitConfiguration.DLQ, message);
            channel.basicAck(deliveryTag, false);
            return;
        }

        try {
            processor.process(parsed.eventId(), parsed.jobId(), parsed.correlationId());
            channel.basicAck(deliveryTag, false);
        } catch (ProviderException exception) {
            if (exception.retryable()) {
                retryOrDeadLetter(parsed, message, channel, deliveryTag, "AI provider call failed");
            } else {
                deadLetter(message, channel, deliveryTag, parsed, "Non-retryable AI provider failure");
            }
        } catch (IllegalArgumentException exception) {
            deadLetter(message, channel, deliveryTag, parsed, "Non-retryable AI job payload failure");
        } catch (RuntimeException exception) {
            retryOrDeadLetter(parsed, message, channel, deliveryTag, "Transient AI job processing failure");
        }
    }

    private ParsedEvent parse(EventEnvelope event) {
        if (event == null || event.eventId() == null || event.version() != 1 || event.payload() == null
                || !event.payload().isObject() || event.payload().get("jobId") == null
                || !event.payload().get("jobId").isTextual()) {
            throw new IllegalArgumentException("Invalid AI generation event envelope");
        }
        try {
            return new ParsedEvent(event.eventId(), UUID.fromString(event.payload().get("jobId").asText()),
                    event.correlationId());
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Invalid AI generation jobId", exception);
        }
    }

    private void retryOrDeadLetter(ParsedEvent event, Message message, Channel channel, long deliveryTag,
                                   String reason) throws IOException {
        int attempt = retryCount(message) + 1;
        message.getMessageProperties().setHeader(RETRY_COUNT, attempt);
        log.warn("{}; jobId={}, attempt={}", reason, event.jobId(), attempt);
        if (attempt >= maxRetries) {
            try {
                processor.failAfterRetries(event.eventId(), event.jobId(), event.correlationId(), reason);
            } catch (RuntimeException failure) {
                log.error("Could not mark exhausted AI job as failed; jobId={}", event.jobId());
            }
            rabbit.send("ai.dlx", RabbitConfiguration.DLQ, message);
        } else {
            rabbit.send("ai.retry.exchange", RabbitConfiguration.RETRY, message);
        }
        channel.basicAck(deliveryTag, false);
    }

    private void deadLetter(Message message, Channel channel, long deliveryTag, ParsedEvent event,
                            String reason) throws IOException {
        log.warn("{}; jobId={}", reason, event.jobId());
        rabbit.send("ai.dlx", RabbitConfiguration.DLQ, message);
        channel.basicAck(deliveryTag, false);
    }

    private int retryCount(Message message) {
        Object count = message.getMessageProperties().getHeaders().getOrDefault(RETRY_COUNT, 0);
        return count instanceof Number number ? number.intValue() : 0;
    }

    private record ParsedEvent(UUID eventId, UUID jobId, UUID correlationId) { }
}
