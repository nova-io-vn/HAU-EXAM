package com.gateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    private static final String[] PUBLIC = {
            "/api/v1/auth/register", "/api/v1/auth/login", "/api/v1/auth/refresh",
            "/api/v1/auth/forgot-password", "/api/v1/auth/verify-otp", "/api/v1/auth/reset-password", "/api/v1/public/faculties", "/api/v1/public/contact",
            "/actuator/health", "/swagger-ui/**", "/v3/api-docs/**", "/ws/**"
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

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    WebFilter corsHeadersFilter(@Value("${CORS_ALLOWED_ORIGINS:http://localhost:5173}") String allowedOrigins) {
        var configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(java.util.Arrays.stream(allowedOrigins.split(","))
                .map(String::trim).filter(origin -> !origin.isBlank()).toList());
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type", "X-Correlation-Id", "Accept"));
        configuration.setExposedHeaders(java.util.List.of("X-Correlation-Id"));
        configuration.setAllowCredentials(true);
        return (exchange, chain) -> {
            var origin = exchange.getRequest().getHeaders().getOrigin();
            if (origin != null && configuration.checkOrigin(origin) != null) {
                var headers = exchange.getResponse().getHeaders();
                headers.setAccessControlAllowOrigin(origin);
                headers.setAccessControlAllowCredentials(true);
                headers.setAccessControlAllowMethods(configuration.getAllowedMethods().stream()
                        .map(org.springframework.http.HttpMethod::valueOf).toList());
                headers.setAccessControlAllowHeaders(configuration.getAllowedHeaders());
                headers.setAccessControlExposeHeaders(configuration.getExposedHeaders());
                if (exchange.getRequest().getMethod() == org.springframework.http.HttpMethod.OPTIONS) {
                    exchange.getResponse().setStatusCode(HttpStatus.OK);
                    return exchange.getResponse().setComplete();
                }
            }
            return chain.filter(exchange);
        };
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
