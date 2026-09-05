package com.hau.gateway;

import com.hau.gateway.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

import static org.mockito.Mockito.when;

@WebFluxTest(controllers = SecurityBoundaryTest.ProbeController.class)
@Import(SecurityConfig.class)
class SecurityBoundaryTest {
    @Autowired WebTestClient client;
    @MockitoBean ReactiveJwtDecoder decoder;

    @Test void publicAuthenticationRouteDoesNotRequireJwt() {
        client.post().uri("/api/v1/auth/login").exchange().expectStatus().value(status ->
                org.assertj.core.api.Assertions.assertThat(status).isNotEqualTo(401));
    }

    @Test void protectedRouteRequiresJwt() {
        client.get().uri("/protected").exchange().expectStatus().isUnauthorized();
    }

    @Test void malformedJwtIsRejected() {
        when(decoder.decode("malformed")).thenReturn(Mono.error(invalid("invalid token")));
        client.get().uri("/protected").headers(h -> h.setBearerAuth("malformed"))
                .exchange().expectStatus().isUnauthorized();
    }

    @Test void expiredJwtIsRejected() {
        when(decoder.decode("expired")).thenReturn(Mono.error(invalid("token expired")));
        client.get().uri("/protected").headers(h -> h.setBearerAuth("expired"))
                .exchange().expectStatus().isUnauthorized();
    }

    @Test void validJwtCanReachProtectedRoute() {
        Jwt jwt = new Jwt("valid", Instant.now(), Instant.now().plusSeconds(60), Map.of("alg", "RS256"), Map.of("sub", "user-id"));
        when(decoder.decode("valid")).thenReturn(Mono.just(jwt));
        client.get().uri("/protected").headers(h -> h.setBearerAuth("valid"))
                .exchange().expectStatus().value(status ->
                        org.assertj.core.api.Assertions.assertThat(status).isNotEqualTo(401));
    }

    private JwtException invalid(String message) {
        return new JwtValidationException(message, java.util.List.of(new OAuth2Error("invalid_token", message, null)));
    }

    @RestController
    static class ProbeController {
        @PostMapping("/api/v1/auth/login") void login() {}
        @GetMapping("/protected") void protectedResource() {}
    }
}
