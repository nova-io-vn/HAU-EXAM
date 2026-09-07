package com.authservice.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Sha256RefreshTokenHasherTest {

    private final Sha256RefreshTokenHasher hasher = new Sha256RefreshTokenHasher();

    @Test
    void hashesAndVerifiesRefreshTokensLongerThanBcryptLimit() {
        String token = "a".repeat(500);
        String hash = hasher.hash(token);

        assertThat(hash).hasSize(64);
        assertThat(hasher.matches(token, hash)).isTrue();
        assertThat(hasher.matches(token + "x", hash)).isFalse();
    }
}
