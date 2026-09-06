package com.userservice.infrastructure.rabbitmq;

import com.userservice.application.port.out.UserEventPublisher;
import com.userservice.domain.model.UserProfile;
import com.userservice.infrastructure.rabbitmq.contract.EventEnvelope;
import com.userservice.infrastructure.rabbitmq.contract.UserChangedPayload;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
public class UserRabbitEventPublisher implements UserEventPublisher {
    private final RabbitTemplate template; private final Clock clock;
    public UserRabbitEventPublisher(RabbitTemplate template,Clock clock){this.template=template;this.clock=clock;}
    public void userApproved(UserProfile u,UUID c){publish("USER_APPROVED","user.approved",u,c);}
    public void userRejected(UserProfile u,UUID c){publish("USER_REJECTED","user.rejected",u,c);}
    public void roleChanged(UserProfile u,UUID c){publish("USER_ROLE_CHANGED","user.role.changed",u,c);}
    public void facultyChanged(UserProfile u,UUID c){publish("USER_FACULTY_CHANGED","user.faculty.changed",u,c);}
    public void statusChanged(UserProfile u,UUID c){publish("USER_STATUS_CHANGED","user.status.changed",u,c);}
    private void publish(String type,String key,UserProfile u,UUID correlationId){
        UUID correlation=correlationId==null?UUID.randomUUID():correlationId;
        var payload=new UserChangedPayload(u.getId(),u.getLecturerCode(),u.getRole(),u.getFacultyId(),u.getStatus(),u.getEmail(),u.getId());
        var envelope=new EventEnvelope<>(UUID.randomUUID(),type,correlation,OffsetDateTime.now(clock).withOffsetSameInstant(ZoneOffset.UTC),1,payload);
        template.convertAndSend(RabbitNames.USER_EXCHANGE,key,envelope,m->{m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);m.getMessageProperties().setCorrelationId(correlation.toString());return m;});
    }
}
