package com.hau.auth.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class BCryptPasswordHasherTest {

    private final BCryptPasswordHasher passwordHasher =
            new BCryptPasswordHasher(new BCryptPasswordEncoder(4));

    @Test
    void hashesAndVerifiesRawPassword() {
        String rawPassword = "valid-password";
        String hash = passwordHasher.hash(rawPassword);

        assertThat(hash).isNotEqualTo(rawPassword);
        assertThat(passwordHasher.matches(rawPassword, hash)).isTrue();
        assertThat(passwordHasher.matches("wrong-password", hash)).isFalse();
    }
}
