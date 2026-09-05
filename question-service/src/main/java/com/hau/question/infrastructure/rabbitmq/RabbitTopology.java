package com.hau.question.infrastructure.rabbitmq;
import java.util.Map; import org.springframework.amqp.core.*; import org.springframework.amqp.support.converter.JacksonJsonMessageConverter; import org.springframework.beans.factory.annotation.Value; import org.springframework.context.annotation.*;
@Configuration public class RabbitTopology {
 public static final String QUESTION_EXCHANGE="question.exchange",AI_EXCHANGE="ai.exchange",QUEUE="question.ai-generation.queue",RETRY="question.ai-generation.retry.queue",DLQ="question.ai-generation.dlq";
 @Bean TopicExchange questionExchange(){return new TopicExchange(QUESTION_EXCHANGE,true,false);}@Bean TopicExchange aiExchange(){return new TopicExchange(AI_EXCHANGE,true,false);}
 @Bean DirectExchange retryExchange(){return new DirectExchange("question.retry.exchange",true,false);}@Bean DirectExchange dlx(){return new DirectExchange("question.dlx",true,false);}
 @Bean Queue aiQueue(){return QueueBuilder.durable(QUEUE).build();}@Bean Queue retryQueue(@Value("${question.rabbitmq.retry-delay-ms}") long delay){return QueueBuilder.durable(RETRY).withArguments(Map.of("x-message-ttl",delay,"x-dead-letter-exchange",AI_EXCHANGE,"x-dead-letter-routing-key","ai.generation.completed")).build();}@Bean Queue dlq(){return QueueBuilder.durable(DLQ).build();}
 @Bean Binding aiBinding(){return BindingBuilder.bind(aiQueue()).to(aiExchange()).with("ai.generation.completed");}@Bean Binding retryBinding(){return BindingBuilder.bind(retryQueue(10000)).to(retryExchange()).with(RETRY);}@Bean Binding dlqBinding(){return BindingBuilder.bind(dlq()).to(dlx()).with(DLQ);}
 @Bean JacksonJsonMessageConverter rabbitJsonMessageConverter(){return new JacksonJsonMessageConverter();}
}
