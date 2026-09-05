package com.hau.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.config.GlobalCorsProperties;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.data.redis.repositories.enabled=false"
})
class GatewayConfigurationTest {
    @Autowired RouteDefinitionLocator routes;
    @Autowired GlobalCorsProperties cors;

    @Test void configuresAllLoadBalancedRoutesAndWebSocket() {
        var definitions = routes.getRouteDefinitions().collectList().block();
        assertThat(definitions).isNotNull();
        Map<String, org.springframework.cloud.gateway.route.RouteDefinition> byId = definitions.stream()
                .collect(Collectors.toMap(org.springframework.cloud.gateway.route.RouteDefinition::getId, Function.identity()));
        assertThat(byId.keySet()).contains("auth-service", "user-service", "question-service", "exam-service",
                "ai-service", "notification-service", "notification-websocket");
        assertThat(byId.get("question-service").getUri().toString()).isEqualTo("lb://question-service");
        assertThat(byId.get("notification-websocket").getUri().toString()).isEqualTo("lb:ws://notification-service");
    }

    @Test void appliesDifferentRedisRateLimitPolicies() {
        var definitions = routes.getRouteDefinitions().collectList().block();
        var byId = definitions.stream().collect(Collectors.toMap(
                org.springframework.cloud.gateway.route.RouteDefinition::getId, Function.identity()));
        var strict = byId.get("auth-sensitive").getFilters().getFirst().getArgs();
        var read = byId.get("notification-service").getFilters().getFirst().getArgs();
        assertThat(strict).containsEntry("redis-rate-limiter.replenishRate", "2")
                .containsEntry("redis-rate-limiter.burstCapacity", "5");
        assertThat(read).containsEntry("redis-rate-limiter.replenishRate", "20")
                .containsEntry("redis-rate-limiter.burstCapacity", "40");
    }

    @Test void corsAllowsOnlyConfiguredFrontendOrigin() {
        var configuration = cors.getCorsConfigurations().get("/**");
        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowedOrigins()).containsExactly("http://localhost:3000");
        assertThat(configuration.getAllowCredentials()).isTrue();
    }
}
