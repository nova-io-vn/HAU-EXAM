package com.userservice.infrastructure.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfiguration {
    @Bean TopicExchange authExchange(){return ExchangeBuilder.topicExchange(RabbitNames.AUTH_EXCHANGE).durable(true).build();}
    @Bean TopicExchange userExchange(){return ExchangeBuilder.topicExchange(RabbitNames.USER_EXCHANGE).durable(true).build();}
    @Bean DirectExchange retryExchange(){return ExchangeBuilder.directExchange(RabbitNames.RETRY_EXCHANGE).durable(true).build();}
    @Bean DirectExchange userDeadLetterExchange(){return ExchangeBuilder.directExchange(RabbitNames.DLX).durable(true).build();}
    @Bean Queue registrationQueue(){return QueueBuilder.durable(RabbitNames.REGISTRATION_QUEUE).deadLetterExchange(RabbitNames.RETRY_EXCHANGE).deadLetterRoutingKey(RabbitNames.RETRY_KEY).build();}
    @Bean Queue registrationRetryQueue(@Value("${user.messaging.retry-delay-ms}") int delay){return QueueBuilder.durable(RabbitNames.RETRY_QUEUE).ttl(delay).deadLetterExchange(RabbitNames.AUTH_EXCHANGE).deadLetterRoutingKey(RabbitNames.REGISTRATION_KEY).build();}
    @Bean Queue registrationDlq(){return QueueBuilder.durable(RabbitNames.DLQ).build();}
    @Bean Binding registrationBinding(Queue registrationQueue,TopicExchange authExchange){return BindingBuilder.bind(registrationQueue).to(authExchange).with(RabbitNames.REGISTRATION_KEY);}
    @Bean Binding retryBinding(Queue registrationRetryQueue,DirectExchange retryExchange){return BindingBuilder.bind(registrationRetryQueue).to(retryExchange).with(RabbitNames.RETRY_KEY);}
    @Bean Binding dlqBinding(Queue registrationDlq,DirectExchange userDeadLetterExchange){return BindingBuilder.bind(registrationDlq).to(userDeadLetterExchange).with(RabbitNames.DLQ_KEY);}
    @Bean JacksonJsonMessageConverter rabbitJsonMessageConverter(){return new JacksonJsonMessageConverter();}
}
