package com.authservice.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.KeyPairGenerator;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class JwtConfigurationTest {
    private final JwtConfiguration configuration = new JwtConfiguration();

    @Test
    void acceptsBase64EncodedPkcs8AndX509DerKeys() throws Exception {
        var pair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        var properties = new JwtProperties();
        properties.setPrivateKeyBase64(Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()));
        properties.setPublicKeyBase64(Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));

        assertDoesNotThrow(() -> configuration.jwtRsaKey(properties));
    }

    @Test
    void rejectsMalformedBase64WithoutEchoingKeyMaterial() {
        var properties = new JwtProperties();
        properties.setPrivateKeyBase64("not-base64-%%%");
        properties.setPublicKeyBase64("also-not-base64-%%%");

        var error = assertThrows(IllegalStateException.class, () -> configuration.jwtRsaKey(properties));

        org.junit.jupiter.api.Assertions.assertTrue(error.getMessage().contains("not valid Base64"));
        org.junit.jupiter.api.Assertions.assertFalse(error.getMessage().contains("not-base64"));
    }

    @Test
    void rejectsBase64EncodedPemText() {
        var properties = new JwtProperties();
        properties.setPrivateKeyBase64(Base64.getEncoder().encodeToString("-----BEGIN PRIVATE KEY-----".getBytes()));
        properties.setPublicKeyBase64(Base64.getEncoder().encodeToString("-----BEGIN PUBLIC KEY-----".getBytes()));

        var error = assertThrows(IllegalStateException.class, () -> configuration.jwtRsaKey(properties));

        org.junit.jupiter.api.Assertions.assertTrue(error.getMessage().contains("Base64-encoded PEM text"));
    }

    @Test
    void rejectsMissingKeysWhenEphemeralGenerationIsDisabled() {
        var properties = new JwtProperties();
        properties.setGenerateEphemeralKey(false);

        var error = assertThrows(IllegalStateException.class, () -> configuration.jwtRsaKey(properties));

        org.junit.jupiter.api.Assertions.assertEquals("JWT RSA private/public keys are required", error.getMessage());
    }
}
