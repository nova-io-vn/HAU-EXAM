package com.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {
    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test void preservesSafeCorrelationId() {
        String id = "93a9a818-3129-4f2f-a640-3fc77893c638";
        assertThat(run(id)).isEqualTo(id);
    }

    @Test void replacesMissingOrAbnormallyLongCorrelationId() {
        String generated = run("x".repeat(65));
        assertThat(generated).isNotEqualTo("x".repeat(65)).hasSize(36);
    }

    @Test void replacesNonUuidCorrelationId() {
        assertThat(run("request-123")).isNotEqualTo("request-123").hasSize(36);
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
