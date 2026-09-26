package com.examservice.infrastructure.rabbitmq;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.*;
@Configuration public class ExamRabbitConfiguration { public static final String EXCHANGE="exam.exchange"; @Bean TopicExchange examExchange(){return new TopicExchange(EXCHANGE,true,false);} }
