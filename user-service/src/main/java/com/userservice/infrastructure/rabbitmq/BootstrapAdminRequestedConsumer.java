package com.userservice.infrastructure.rabbitmq;

import com.rabbitmq.client.Channel;
import com.userservice.application.service.SystemAdminBootstrapService;
import com.userservice.infrastructure.rabbitmq.contract.BootstrapAdminRequestedPayload;
import com.userservice.infrastructure.rabbitmq.contract.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class BootstrapAdminRequestedConsumer {
    private static final Logger log = LoggerFactory.getLogger(BootstrapAdminRequestedConsumer.class);
    private static final long MAX_ATTEMPTS = 3;
    private final SystemAdminBootstrapService service;
    private final RabbitTemplate template;
    public BootstrapAdminRequestedConsumer(SystemAdminBootstrapService service, RabbitTemplate template) { this.service = service; this.template = template; }
    @RabbitListener(queues = RabbitNames.BOOTSTRAP_QUEUE)
    public void consume(EventEnvelope<BootstrapAdminRequestedPayload> event, Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        try {
            if (event == null || event.eventId() == null || event.payload() == null) throw new IllegalArgumentException("Invalid bootstrap admin event envelope");
            service.createIfAbsent(event.eventId(), event.payload());
            channel.basicAck(tag, false);
        } catch (Exception ex) {
            long attempts = attempts(message);
            log.warn("Bootstrap admin event processing failed; eventId={}, attempt={}", event == null ? null : event.eventId(), attempts + 1);
            if (attempts + 1 >= MAX_ATTEMPTS) { template.send(RabbitNames.DLX, RabbitNames.BOOTSTRAP_DLQ_KEY, message); channel.basicAck(tag, false); }
            else channel.basicNack(tag, false, false);
        }
    }
    private long attempts(Message message) {
        List<Map<String, ?>> deaths = message.getMessageProperties().getXDeathHeader();
        if (deaths == null || deaths.isEmpty()) return 0;
        Object count = deaths.getFirst().get("count");
        return count instanceof Number n ? n.longValue() : 0;
    }
}
