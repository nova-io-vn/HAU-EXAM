package com.authservice.infrastructure.messaging;

import com.authservice.application.dto.SecuritySnapshotUpdate;
import com.authservice.application.service.SecuritySnapshotService;
import com.authservice.infrastructure.config.RabbitConfiguration;
import com.authservice.infrastructure.messaging.contract.UserEventEnvelope;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class UserSecuritySnapshotConsumer {
    private static final Logger log = LoggerFactory.getLogger(UserSecuritySnapshotConsumer.class);

    private final SecuritySnapshotService snapshots;
    private final RabbitTemplate rabbitTemplate;
    private final int maxRetries;

    public UserSecuritySnapshotConsumer(SecuritySnapshotService snapshots, RabbitTemplate rabbitTemplate,
                                        @Value("${auth.messaging.max-retries:3}") int maxRetries) {
        this.snapshots = snapshots;
        this.rabbitTemplate = rabbitTemplate;
        this.maxRetries = maxRetries;
    }

    @RabbitListener(queues = RabbitConfiguration.USER_SECURITY_QUEUE)
    public void consume(UserEventEnvelope event, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            var payload = event == null ? null : event.payload();
            snapshots.synchronize(new SecuritySnapshotUpdate(
                    event == null ? null : event.eventId(),
                    event == null ? null : event.eventType(),
                    event == null ? 0 : event.version(),
                    payload == null ? null : payload.userId(),
                    payload == null ? null : payload.lecturerCode(),
                    payload == null ? null : payload.status(),
                    payload == null ? null : payload.role(),
                    payload == null ? null : payload.facultyId(),
                    payload == null ? null : payload.email()));
            channel.basicAck(deliveryTag, false);
        } catch (IllegalArgumentException exception) {
            log.warn("Rejecting malformed user security event; eventId={}", event == null ? null : event.eventId());
            rabbitTemplate.send(RabbitConfiguration.AUTH_DLX, RabbitConfiguration.USER_SECURITY_DLQ_KEY, message);
            channel.basicAck(deliveryTag, false);
        } catch (Exception exception) {
            long attempt = attempts(message) + 1;
            log.warn("User security synchronization failed; eventId={}, attempt={}",
                    event == null ? null : event.eventId(), attempt);
            if (attempt >= maxRetries) {
                rabbitTemplate.send(RabbitConfiguration.AUTH_DLX, RabbitConfiguration.USER_SECURITY_DLQ_KEY, message);
                channel.basicAck(deliveryTag, false);
            } else {
                channel.basicNack(deliveryTag, false, false);
            }
        }
    }

    private long attempts(Message message) {
        List<Map<String, ?>> deaths = message.getMessageProperties().getXDeathHeader();
        if (deaths == null || deaths.isEmpty()) return 0;
        Object count = deaths.getFirst().get("count");
        return count instanceof Number number ? number.longValue() : 0;
    }
}
