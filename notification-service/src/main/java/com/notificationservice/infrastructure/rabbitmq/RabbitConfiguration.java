package com.notificationservice.infrastructure.rabbitmq;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
@Configuration public class RabbitConfiguration {
 public static final String QUEUE="notification.events.queue",RETRY_QUEUE="notification.events.retry.queue",DLQ="notification.events.dlq",RETRY_EX="notification.retry.exchange",DLX="notification.dlx";
 @Bean TopicExchange authExchange(){return new TopicExchange("auth.exchange",true,false);}
 @Bean TopicExchange userExchange(){return new TopicExchange("user.exchange",true,false);}
 @Bean TopicExchange questionExchange(){return new TopicExchange("question.exchange",true,false);}
 @Bean TopicExchange aiExchange(){return new TopicExchange("ai.exchange",true,false);}
 @Bean TopicExchange examExchange(){return new TopicExchange("exam.exchange",true,false);}
 @Bean DirectExchange retryExchange(){return new DirectExchange(RETRY_EX,true,false);}
 @Bean DirectExchange dlx(){return new DirectExchange(DLX,true,false);}
 @Bean Queue eventQueue(){return QueueBuilder.durable(QUEUE).deadLetterExchange(RETRY_EX).deadLetterRoutingKey("retry").build();}
 @Bean Queue retryQueue(@Value("${notification.retry-delay-ms:5000}")int ttl){return QueueBuilder.durable(RETRY_QUEUE).ttl(ttl).deadLetterExchange("").deadLetterRoutingKey(QUEUE).build();}
 @Bean Queue dlq(){return QueueBuilder.durable(DLQ).build();}
 @Bean Binding retryBinding(Queue retryQueue,DirectExchange retryExchange){return BindingBuilder.bind(retryQueue).to(retryExchange).with("retry");}
 @Bean Binding dlqBinding(Queue dlq,DirectExchange dlx){return BindingBuilder.bind(dlq).to(dlx).with("dead");}
 @Bean Declarables eventBindings(Queue eventQueue,TopicExchange authExchange,TopicExchange userExchange,TopicExchange questionExchange,TopicExchange aiExchange,TopicExchange examExchange){return new Declarables(
  BindingBuilder.bind(eventQueue).to(authExchange).with("password.reset.otp.requested"),BindingBuilder.bind(eventQueue).to(authExchange).with("user.login.success"),BindingBuilder.bind(eventQueue).to(authExchange).with("user.registration.requested"),
  BindingBuilder.bind(eventQueue).to(userExchange).with("user.approved"),BindingBuilder.bind(eventQueue).to(userExchange).with("user.rejected"),BindingBuilder.bind(eventQueue).to(userExchange).with("user.role.changed"),BindingBuilder.bind(eventQueue).to(userExchange).with("user.faculty.changed"),BindingBuilder.bind(eventQueue).to(userExchange).with("user.status.changed"),
  BindingBuilder.bind(eventQueue).to(questionExchange).with("question.submitted"),BindingBuilder.bind(eventQueue).to(questionExchange).with("question.approved"),BindingBuilder.bind(eventQueue).to(questionExchange).with("question.rejected"),BindingBuilder.bind(eventQueue).to(questionExchange).with("question.revision.requested"),BindingBuilder.bind(eventQueue).to(aiExchange).with("ai.generation.completed"),BindingBuilder.bind(eventQueue).to(aiExchange).with("ai.generation.failed"),BindingBuilder.bind(eventQueue).to(examExchange).with("exam.generated"));}
 @Bean JacksonJsonMessageConverter converter(){return new JacksonJsonMessageConverter();}
}
