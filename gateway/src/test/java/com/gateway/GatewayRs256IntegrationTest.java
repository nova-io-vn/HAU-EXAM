package com.gateway;

import com.gateway.config.SecurityConfig;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.UUID;

@WebFluxTest
@Import({SecurityConfig.class, GatewayRs256IntegrationTest.JwtTestConfiguration.class,
        GatewayRs256IntegrationTest.ProtectedProbe.class})
class GatewayRs256IntegrationTest {
    @Autowired WebTestClient client;
    @Autowired RSAKey rsaKey;

    @Test
    void authStyleRs256TokenPassesGatewayAndReachesProtectedRoute() {
        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder().issuer("hau-exam-auth").subject(UUID.randomUUID().toString())
                .issuedAt(now).expiresAt(now.plusSeconds(300)).id(UUID.randomUUID().toString())
                .claim("lecturerCode", "GV001").claim("role", "SUBJECT_ADMIN").claim("facultyId", "CNTT")
                .claim("tokenType", "access").build();
        var encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey)));
        String token = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(SignatureAlgorithm.RS256).keyId(rsaKey.getKeyID()).build(), claims)).getTokenValue();

        client.get().uri("/protected-downstream").headers(headers -> headers.setBearerAuth(token))
                .exchange().expectStatus().isOk();
    }

    @RestController
    public static class ProtectedProbe {
        @GetMapping("/protected-downstream") public String protectedDownstream() { return "ok"; }
    }

    @TestConfiguration
    static class JwtTestConfiguration {
        @Bean RSAKey rsaKey() throws Exception {
            var generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            var pair = generator.generateKeyPair();
            return new RSAKey.Builder((RSAPublicKey) pair.getPublic())
                    .privateKey((RSAPrivateKey) pair.getPrivate()).keyID("integration-test-key").build();
        }

        @Bean ReactiveJwtDecoder reactiveJwtDecoder(RSAKey key) throws Exception {
            return NimbusReactiveJwtDecoder.withPublicKey(key.toRSAPublicKey())
                    .signatureAlgorithm(SignatureAlgorithm.RS256).build();
        }
    }
}
