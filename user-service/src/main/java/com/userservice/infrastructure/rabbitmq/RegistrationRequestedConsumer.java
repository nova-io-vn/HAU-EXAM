package com.userservice.infrastructure.rabbitmq;

import com.userservice.application.dto.RegistrationCommand;
import com.userservice.application.port.in.RegistrationUseCase;
import com.userservice.infrastructure.rabbitmq.contract.EventEnvelope;
import com.userservice.infrastructure.rabbitmq.contract.RegistrationRequestedPayload;
import com.rabbitmq.client.Channel;
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
public class RegistrationRequestedConsumer {
    private static final Logger log=LoggerFactory.getLogger(RegistrationRequestedConsumer.class);
    private static final long MAX_ATTEMPTS=3;
    private final RegistrationUseCase useCase; private final RabbitTemplate template;
    public RegistrationRequestedConsumer(RegistrationUseCase useCase,RabbitTemplate template){this.useCase=useCase;this.template=template;}
    @RabbitListener(queues=RabbitNames.REGISTRATION_QUEUE)
    public void consume(EventEnvelope<RegistrationRequestedPayload> event, Message message, Channel channel) throws IOException {
        long tag=message.getMessageProperties().getDeliveryTag();
        try {
            if(event==null||event.eventId()==null||event.payload()==null) throw new IllegalArgumentException("Invalid registration event envelope");
            var p=event.payload();
            useCase.createFromRegistration(new RegistrationCommand(event.eventId(),event.correlationId(),p.userId(),p.lecturerCode(),p.fullName(),p.dateOfBirth(),p.phone(),p.email(),p.address(),p.avatar(),p.facultyId()));
            channel.basicAck(tag,false);
        } catch(Exception ex) {
            long attempts=attempts(message);
            log.warn("Registration event processing failed; eventId={}, attempt={}",event==null?null:event.eventId(),attempts+1);
            if(attempts+1>=MAX_ATTEMPTS){template.send(RabbitNames.DLX,RabbitNames.DLQ_KEY,message);channel.basicAck(tag,false);} else channel.basicNack(tag,false,false);
        }
    }
    private long attempts(Message message){
        List<Map<String,?>> deaths=message.getMessageProperties().getXDeathHeader();
        if(deaths==null||deaths.isEmpty()) return 0;
        Object count=deaths.getFirst().get("count"); return count instanceof Number n?n.longValue():0;
    }
}
