package com.aiservice.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InternalServiceTokenVerifierTest {
    @Test
    void acceptsOnlyTheConfiguredToken() {
        var verifier = new InternalServiceTokenVerifier("test-internal-token");
        assertThat(verifier.matches("test-internal-token")).isTrue();
        assertThat(verifier.matches("wrong-token")).isFalse();
        assertThat(verifier.matches(null)).isFalse();
    }

    @Test
    void refusesToStartWithoutAConfiguredToken() {
        assertThatThrownBy(() -> new InternalServiceTokenVerifier(" "))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("INTERNAL_SERVICE_TOKEN");
    }
}
