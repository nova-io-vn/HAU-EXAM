package com.hau.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class IdentityHeaderFilter implements GlobalFilter, Ordered {
    static final String USER_ID = "X-User-Id";
    static final String LECTURER_CODE = "X-Lecturer-Code";
    static final String ROLE = "X-Role";
    static final String FACULTY_ID = "X-Faculty-Id";
    private static final List<String> IDENTITY_HEADERS = List.of(USER_ID, LECTURER_CODE, ROLE, FACULTY_ID);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerWebExchange sanitized = mutate(exchange, null);
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .filter(JwtAuthenticationToken.class::isInstance)
                .cast(JwtAuthenticationToken.class)
                .map(token -> mutate(sanitized, token))
                .defaultIfEmpty(sanitized)
                .flatMap(chain::filter);
    }

    private ServerWebExchange mutate(ServerWebExchange exchange, JwtAuthenticationToken token) {
        return exchange.mutate().request(exchange.getRequest().mutate().headers(headers -> {
            IDENTITY_HEADERS.forEach(headers::remove);
            if (token != null) {
                set(headers, USER_ID, token.getToken().getSubject());
                set(headers, LECTURER_CODE, token.getToken().getClaimAsString("lecturerCode"));
                set(headers, ROLE, token.getToken().getClaimAsString("role"));
                set(headers, FACULTY_ID, token.getToken().getClaimAsString("facultyId"));
            }
        }).build()).build();
    }

    private void set(HttpHeaders headers, String name, String value) { if (value != null && !value.isBlank()) headers.set(name, value); }
    @Override public int getOrder() { return Ordered.HIGHEST_PRECEDENCE + 10; }
}
