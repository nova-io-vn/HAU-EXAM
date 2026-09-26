package com.examservice.infrastructure.rabbitmq;
import com.examservice.application.port.out.ExamEventPublisher;
import com.examservice.domain.model.Exam;
import java.time.*;import java.util.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
@Component public class ExamRabbitEventPublisher implements ExamEventPublisher {
 private final RabbitTemplate rabbit; public ExamRabbitEventPublisher(RabbitTemplate rabbit){this.rabbit=rabbit;}
 public void generated(Exam exam,UUID requestedBy){var payload=new LinkedHashMap<String,Object>();payload.put("examId",exam.id());payload.put("createdBy",exam.createdBy());payload.put("requestedBy",requestedBy==null?exam.createdBy():requestedBy);payload.put("facultyId",exam.facultyId());payload.put("subjectId",exam.subjectId());payload.put("name",exam.name());rabbit.convertAndSend(ExamRabbitConfiguration.EXCHANGE,"exam.generated",new EventEnvelope(UUID.randomUUID(),"EXAM_GENERATED",UUID.randomUUID(),OffsetDateTime.now(ZoneOffset.UTC),1,payload));}
 public record EventEnvelope(UUID eventId,String eventType,UUID correlationId,OffsetDateTime occurredAt,int version,Object payload){}
}
