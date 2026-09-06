package com.examservice.infrastructure.config;

import java.time.Clock;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ApplicationConfig {
    @Bean Clock clock() { return Clock.systemUTC(); }

    @Bean
    @Primary
    RestClient.Builder restClientBuilder() { return RestClient.builder(); }

    @Bean
    @LoadBalanced
    RestClient.Builder loadBalancedRestClientBuilder() { return RestClient.builder(); }
}
