package com.aiservice.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class InternalServiceTokenVerifier {
    private final byte[] expectedToken;

    public InternalServiceTokenVerifier(@Value("${ai.internal.result-token}") String configuredToken) {
        if (configuredToken == null || configuredToken.isBlank()) {
            throw new IllegalStateException("INTERNAL_SERVICE_TOKEN is required for AI internal result access");
        }
        this.expectedToken = configuredToken.getBytes(StandardCharsets.UTF_8);
    }

    public boolean matches(String presentedToken) {
        return presentedToken != null && MessageDigest.isEqual(
                expectedToken, presentedToken.getBytes(StandardCharsets.UTF_8));
    }
}
