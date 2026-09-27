package com.questionservice.infrastructure.security;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
@Component
public class InternalServiceTokenVerifier {
    private final byte[] expected;
    public InternalServiceTokenVerifier(@Value("${question.internal.service-token:${INTERNAL_SERVICE_TOKEN:dev-internal-token}}") String token) { expected = token.getBytes(StandardCharsets.UTF_8); }
    public boolean matches(String token) { return token != null && MessageDigest.isEqual(expected, token.getBytes(StandardCharsets.UTF_8)); }
}
