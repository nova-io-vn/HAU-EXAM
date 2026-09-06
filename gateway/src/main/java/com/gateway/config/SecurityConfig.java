package com.gateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    private static final String[] PUBLIC = {
            "/api/v1/auth/register", "/api/v1/auth/login", "/api/v1/auth/refresh",
            "/api/v1/auth/forgot-password", "/api/v1/auth/verify-otp", "/api/v1/auth/reset-password",
            "/actuator/health", "/swagger-ui/**", "/v3/api-docs/**"
    };

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, ObjectMapper mapper) {
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(org.springframework.http.HttpMethod.OPTIONS).permitAll()
                        .pathMatchers(PUBLIC).permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> {})
                        .authenticationEntryPoint((exchange, error) -> error(exchange, mapper,
                                HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication is required or token is invalid")))
                .exceptionHandling(errors -> errors
                        .accessDeniedHandler((exchange, error) -> error(exchange, mapper,
                                HttpStatus.FORBIDDEN, "FORBIDDEN", "Access is denied")))
                .build();
    }

    private Mono<Void> error(ServerWebExchange exchange, ObjectMapper mapper, HttpStatus status,
                             String code, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-Id");
        byte[] bytes;
        try {
            bytes = mapper.writeValueAsBytes(Map.of("success", false, "code", code, "message", message,
                    "data", Map.of(), "correlationId", correlationId == null ? "" : correlationId));
        } catch (JsonProcessingException ignored) {
            bytes = ("{\"success\":false,\"code\":\"" + code + "\",\"message\":\"" + message + "\"}")
                    .getBytes(StandardCharsets.UTF_8);
        }
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
