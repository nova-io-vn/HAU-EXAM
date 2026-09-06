package com.questionservice.infrastructure.rabbitmq;
import tools.jackson.databind.ObjectMapper;
import com.questionservice.application.service.AiQuestionImportService;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import org.slf4j.*;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component public class AiGenerationConsumer {
 private static final Logger log=LoggerFactory.getLogger(AiGenerationConsumer.class);
 private final ObjectMapper mapper; private final AiQuestionImportService service; private final RabbitTemplate rabbit; private final int maxRetries;
 public AiGenerationConsumer(ObjectMapper m,AiQuestionImportService s,RabbitTemplate r,@Value("${question.rabbitmq.max-retries}")int max){mapper=m;service=s;rabbit=r;maxRetries=max;}
 @RabbitListener(queues=RabbitTopology.QUEUE) public void consume(EventEnvelope event,Message message,Channel channel)throws IOException{long tag=message.getMessageProperties().getDeliveryTag();try{if(event==null||event.eventId()==null||event.payload()==null)throw new IllegalArgumentException("Invalid AI event envelope");AiGenerationPayload payload=mapper.treeToValue(event.payload(),AiGenerationPayload.class);service.importCompleted(payload,event.eventId());channel.basicAck(tag,false);}catch(Exception ex){Number previous=(Number)message.getMessageProperties().getHeaders().getOrDefault("x-retry-count",0);int count=previous.intValue()+1;message.getMessageProperties().setHeader("x-retry-count",count);log.warn("AI import failed; eventId={}, attempt={}, correlationId={}",event==null?null:event.eventId(),count,event==null?null:event.correlationId());if(count>=maxRetries)rabbit.send("question.dlx",RabbitTopology.DLQ,message);else rabbit.send("question.retry.exchange",RabbitTopology.RETRY,message);channel.basicAck(tag,false);}}
}
