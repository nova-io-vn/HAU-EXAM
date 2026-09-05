package com.hau.user.infrastructure.rabbitmq;

import com.hau.user.domain.model.UserProfile;
import com.hau.user.infrastructure.rabbitmq.contract.EventEnvelope;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.time.*;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserRabbitEventPublisherTest {
    @Test void publishesApprovalToExpectedExchangeAndRoutingKey(){
        RabbitTemplate template=mock(RabbitTemplate.class);
        var publisher=new UserRabbitEventPublisher(template,Clock.fixed(Instant.parse("2026-09-05T00:00:00Z"),ZoneOffset.UTC));
        var user=UserProfile.pending(UUID.randomUUID(),"GV1","User",null,null,"u@hau.edu.vn",null,null,null,Instant.parse("2026-09-04T00:00:00Z")).approve(Instant.parse("2026-09-05T00:00:00Z"));
        publisher.userApproved(user,UUID.randomUUID());
        verify(template).convertAndSend(eq(RabbitNames.USER_EXCHANGE),eq("user.approved"),any(EventEnvelope.class),any(MessagePostProcessor.class));
    }
}
