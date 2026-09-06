package com.authservice.infrastructure.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.JOSEException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfiguration {
    @Bean
    RSAKey jwtRsaKey(JwtProperties properties) {
        try {
            RSAPublicKey publicKey;
            RSAPrivateKey privateKey;
            boolean hasPrivateKey = hasText(properties.getPrivateKeyBase64());
            boolean hasPublicKey = hasText(properties.getPublicKeyBase64());
            if (hasPrivateKey != hasPublicKey) {
                throw new IllegalStateException("JWT RSA private/public keys must be provided together");
            }
            if (hasPrivateKey) {
                KeyFactory factory = KeyFactory.getInstance("RSA");
                privateKey = (RSAPrivateKey) factory.generatePrivate(new PKCS8EncodedKeySpec(
                        decodeDer(properties.getPrivateKeyBase64(), "private", "PKCS#8")));
                publicKey = (RSAPublicKey) factory.generatePublic(new X509EncodedKeySpec(
                        decodeDer(properties.getPublicKeyBase64(), "public", "X.509 SubjectPublicKeyInfo")));
            } else if (properties.isGenerateEphemeralKey()) {
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
                generator.initialize(2048);
                var pair = generator.generateKeyPair();
                publicKey = (RSAPublicKey) pair.getPublic();
                privateKey = (RSAPrivateKey) pair.getPrivate();
            } else {
                throw new IllegalStateException("JWT RSA private/public keys are required");
            }
            return new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(properties.getKeyId()).build();
        } catch (Exception exception) {
            if (exception instanceof IllegalStateException illegalStateException) {
                throw illegalStateException;
            }
            throw new IllegalStateException("Unable to configure JWT RSA key pair: JWT_PRIVATE_KEY_BASE64 and JWT_PUBLIC_KEY_BASE64 must contain Base64-encoded DER (PKCS#8 private, X.509 public) keys", exception);
        }
    }

    @Bean JwtEncoder jwtEncoder(RSAKey key) { return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(key))); }
    @Bean JwtDecoder jwtDecoder(RSAKey key) throws JOSEException {
        return NimbusJwtDecoder.withPublicKey(key.toRSAPublicKey()).signatureAlgorithm(SignatureAlgorithm.RS256).build();
    }

    private boolean hasText(String value) { return value != null && !value.isBlank(); }

    private byte[] decodeDer(String encoded, String keyName, String format) {
        final byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(encoded.trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("JWT " + keyName + " key is not valid Base64; expected Base64-encoded " + format + " DER", exception);
        }
        String decodedText = new String(decoded, StandardCharsets.US_ASCII);
        if (decodedText.contains("-----BEGIN") || decodedText.contains("-----END")) {
            throw new IllegalStateException("JWT " + keyName + " key must be Base64-encoded " + format + " DER, not Base64-encoded PEM text");
        }
        return decoded;
    }
}
