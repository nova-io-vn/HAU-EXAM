package com.hau.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {
    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test void preservesSafeCorrelationId() {
        assertThat(run("request-123")).isEqualTo("request-123");
    }

    @Test void replacesMissingOrAbnormallyLongCorrelationId() {
        String generated = run("x".repeat(65));
        assertThat(generated).isNotEqualTo("x".repeat(65)).hasSize(36);
    }

    private String run(String supplied) {
        var request = MockServerHttpRequest.get("/api/v1/questions");
        if (supplied != null) request.header(CorrelationIdFilter.HEADER, supplied);
        var exchange = MockServerWebExchange.from(request.build());
        AtomicReference<String> downstream = new AtomicReference<>();
        filter.filter(exchange, filtered -> {
            downstream.set(filtered.getRequest().getHeaders().getFirst(CorrelationIdFilter.HEADER));
            return reactor.core.publisher.Mono.empty();
        }).block();
        assertThat(exchange.getResponse().getHeaders().getFirst(CorrelationIdFilter.HEADER)).isEqualTo(downstream.get());
        return downstream.get();
    }
}
