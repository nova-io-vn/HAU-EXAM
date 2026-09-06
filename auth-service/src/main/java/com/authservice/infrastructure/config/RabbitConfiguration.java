package com.authservice.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {
    public static final String USER_EXCHANGE = "user.exchange";
    public static final String AUTH_RETRY_EXCHANGE = "auth.retry.exchange";
    public static final String AUTH_DLX = "auth.dlx";
    public static final String USER_SECURITY_QUEUE = "auth.user.security.queue";
    public static final String USER_SECURITY_RETRY_QUEUE = "auth.user.security.retry.queue";
    public static final String USER_SECURITY_RETRY_KEY = "auth.user.security.retry";
    public static final String USER_SECURITY_DLQ = "auth.user.security.dlq";
    public static final String USER_SECURITY_DLQ_KEY = "auth.user.security.dead";

    @Bean TopicExchange authExchange() { return new TopicExchange("auth.exchange", true, false); }
    @Bean TopicExchange userExchange() { return ExchangeBuilder.topicExchange(USER_EXCHANGE).durable(true).build(); }
    @Bean DirectExchange authRetryExchange() { return ExchangeBuilder.directExchange(AUTH_RETRY_EXCHANGE).durable(true).build(); }
    @Bean DirectExchange authDeadLetterExchange() { return ExchangeBuilder.directExchange(AUTH_DLX).durable(true).build(); }
    @Bean Queue userSecurityQueue() {
        return QueueBuilder.durable(USER_SECURITY_QUEUE)
                .deadLetterExchange(AUTH_RETRY_EXCHANGE).deadLetterRoutingKey(USER_SECURITY_RETRY_KEY).build();
    }
    @Bean Queue userSecurityRetryQueue(@Value("${auth.messaging.retry-delay-ms:5000}") int delay) {
        return QueueBuilder.durable(USER_SECURITY_RETRY_QUEUE).ttl(delay)
                .deadLetterExchange(USER_EXCHANGE).deadLetterRoutingKey("user.security.replay").build();
    }
    @Bean Queue userSecurityDlq() { return QueueBuilder.durable(USER_SECURITY_DLQ).build(); }
    @Bean Binding approvedBinding(Queue userSecurityQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userSecurityQueue).to(userExchange).with("user.approved");
    }
    @Bean Binding rejectedBinding(Queue userSecurityQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userSecurityQueue).to(userExchange).with("user.rejected");
    }
    @Bean Binding statusBinding(Queue userSecurityQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userSecurityQueue).to(userExchange).with("user.status.changed");
    }
    @Bean Binding roleBinding(Queue userSecurityQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userSecurityQueue).to(userExchange).with("user.role.changed");
    }
    @Bean Binding facultyBinding(Queue userSecurityQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userSecurityQueue).to(userExchange).with("user.faculty.changed");
    }
    @Bean Binding replayBinding(Queue userSecurityQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userSecurityQueue).to(userExchange).with("user.security.replay");
    }
    @Bean Binding userSecurityRetryBinding(Queue userSecurityRetryQueue, DirectExchange authRetryExchange) {
        return BindingBuilder.bind(userSecurityRetryQueue).to(authRetryExchange).with(USER_SECURITY_RETRY_KEY);
    }
    @Bean Binding userSecurityDlqBinding(Queue userSecurityDlq, DirectExchange authDeadLetterExchange) {
        return BindingBuilder.bind(userSecurityDlq).to(authDeadLetterExchange).with(USER_SECURITY_DLQ_KEY);
    }
    @Bean JacksonJsonMessageConverter rabbitJsonMessageConverter() { return new JacksonJsonMessageConverter(); }
}
