package com.hau.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long started = System.nanoTime();
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getPath().value();
        String correlation = exchange.getRequest().getHeaders().getFirst(CorrelationIdFilter.HEADER);
        return chain.filter(exchange)
                .doOnSuccess(ignored -> log.info("Gateway request method={} path={} status={} durationMs={} correlationId={}",
                        method, path, exchange.getResponse().getStatusCode(), elapsed(started), correlation))
                .doOnError(error -> log.warn("Gateway request failed method={} path={} durationMs={} correlationId={} errorType={}",
                        method, path, elapsed(started), correlation, error.getClass().getSimpleName()));
    }

    private long elapsed(long started) { return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started); }
    @Override public int getOrder() { return Ordered.LOWEST_PRECEDENCE; }
}
