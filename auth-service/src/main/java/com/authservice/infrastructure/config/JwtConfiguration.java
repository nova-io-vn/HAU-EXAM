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

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfiguration {
    @Bean
    RSAKey jwtRsaKey(JwtProperties properties) {
        try {
            RSAPublicKey publicKey;
            RSAPrivateKey privateKey;
            if (hasText(properties.getPrivateKeyBase64()) && hasText(properties.getPublicKeyBase64())) {
                KeyFactory factory = KeyFactory.getInstance("RSA");
                privateKey = (RSAPrivateKey) factory.generatePrivate(new PKCS8EncodedKeySpec(
                        Base64.getDecoder().decode(properties.getPrivateKeyBase64())));
                publicKey = (RSAPublicKey) factory.generatePublic(new X509EncodedKeySpec(
                        Base64.getDecoder().decode(properties.getPublicKeyBase64())));
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
            throw new IllegalStateException("Unable to configure JWT RSA key pair", exception);
        }
    }

    @Bean JwtEncoder jwtEncoder(RSAKey key) { return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(key))); }
    @Bean JwtDecoder jwtDecoder(RSAKey key) throws JOSEException {
        return NimbusJwtDecoder.withPublicKey(key.toRSAPublicKey()).signatureAlgorithm(SignatureAlgorithm.RS256).build();
    }

    private boolean hasText(String value) { return value != null && !value.isBlank(); }
}
