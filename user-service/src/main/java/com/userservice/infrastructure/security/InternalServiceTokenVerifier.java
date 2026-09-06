package com.userservice.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class InternalServiceTokenVerifier {
    private final byte[] expected;
    public InternalServiceTokenVerifier(@Value("${user.internal.service-token}") String token) {
        if (token == null || token.isBlank()) throw new IllegalStateException("INTERNAL_SERVICE_TOKEN is required");
        expected = token.getBytes(StandardCharsets.UTF_8);
    }
    public boolean matches(String token) {
        return token != null && MessageDigest.isEqual(expected, token.getBytes(StandardCharsets.UTF_8));
    }
}
