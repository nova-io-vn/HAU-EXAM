package com.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class IdentityHeaderFilterTest {
    private final IdentityHeaderFilter filter = new IdentityHeaderFilter();

    @Test void removesSpoofedIdentityHeadersForAnonymousRequest() {
        HttpHeaders headers = run(null);
        assertThat(headers.containsHeader(IdentityHeaderFilter.USER_ID)).isFalse();
        assertThat(headers.containsHeader(IdentityHeaderFilter.ROLE)).isFalse();
    }

    @Test void overwritesSpoofedHeadersWithValidatedJwtClaims() {
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("alg", "none"), Map.of("sub", "real-user", "lecturerCode", "GV001",
                        "role", "USER", "facultyId", "CNTT"));
        HttpHeaders headers = run(new JwtAuthenticationToken(jwt));
        assertThat(headers.getFirst(IdentityHeaderFilter.USER_ID)).isEqualTo("real-user");
        assertThat(headers.getFirst(IdentityHeaderFilter.LECTURER_CODE)).isEqualTo("GV001");
        assertThat(headers.getFirst(IdentityHeaderFilter.ROLE)).isEqualTo("USER");
        assertThat(headers.getFirst(IdentityHeaderFilter.FACULTY_ID)).isEqualTo("CNTT");
    }

    private HttpHeaders run(JwtAuthenticationToken authentication) {
        var request = MockServerHttpRequest.get("/api/v1/questions")
                .header(IdentityHeaderFilter.USER_ID, "spoofed")
                .header(IdentityHeaderFilter.ROLE, "SYSTEM_ADMIN").build();
        AtomicReference<HttpHeaders> captured = new AtomicReference<>();
        var execution = filter.filter(MockServerWebExchange.from(request), exchange -> {
            captured.set(exchange.getRequest().getHeaders());
            return reactor.core.publisher.Mono.empty();
        });
        if (authentication != null) execution = execution.contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
        execution.block();
        return captured.get();
    }
}
