package com.gateway.presence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@ConditionalOnBean(PresenceService.class)
public class PresenceWebFilter implements WebFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(PresenceWebFilter.class);
    private final PresenceService presence;

    public PresenceWebFilter(PresenceService presence) { this.presence = presence; }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .filter(JwtAuthenticationToken.class::isInstance)
                .cast(JwtAuthenticationToken.class)
                .flatMap(token -> presence.touch(token.getToken().getSubject())
                        .onErrorResume(error -> {
                            log.warn("Presence heartbeat failed; errorType={}", error.getClass().getSimpleName());
                            return Mono.empty();
                        })
                        .then(chain.filter(exchange)))
                .switchIfEmpty(chain.filter(exchange));
    }

    @Override public int getOrder() { return Ordered.LOWEST_PRECEDENCE - 100; }
}
