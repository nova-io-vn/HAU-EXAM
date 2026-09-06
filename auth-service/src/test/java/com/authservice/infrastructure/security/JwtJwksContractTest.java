package com.authservice.infrastructure.security;

import com.authservice.application.port.out.TokenService;
import com.authservice.domain.model.AccountStatus;
import com.authservice.domain.model.AuthAccount;
import com.authservice.presentation.controller.JwksController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class JwtJwksContractTest {
    @Autowired TokenService tokens;
    @Autowired JwtDecoder decoder;
    @Autowired JwksController jwks;

    @Test
    void issuedRs256AccessTokenCarriesSynchronizedClaimsAndJwksIsPublicOnly() {
        UUID userId = UUID.randomUUID();
        AuthAccount account = new AuthAccount(userId, "GV001", "hash", AccountStatus.ACTIVE,
                "SUBJECT_ADMIN", "CNTT", "gv001@hau.edu.vn", Instant.now(), Instant.now(), 0);

        var issued = tokens.issue(account);
        var jwt = decoder.decode(issued.accessToken());

        assertThat(jwt.getHeaders()).containsEntry("alg", "RS256");
        assertThat(jwt.getSubject()).isEqualTo(userId.toString());
        assertThat(jwt.getClaimAsString("lecturerCode")).isEqualTo("GV001");
        assertThat(jwt.getClaimAsString("role")).isEqualTo("SUBJECT_ADMIN");
        assertThat(jwt.getClaimAsString("facultyId")).isEqualTo("CNTT");
        assertThat(jwt.getId()).isNotBlank();
        assertThat(jwt.getIssuedAt()).isNotNull();
        assertThat(jwt.getExpiresAt()).isNotNull();

        @SuppressWarnings("unchecked")
        Map<String, Object> publicJwk = (Map<String, Object>) ((List<?>) jwks.jwks().get("keys")).getFirst();
        assertThat(publicJwk).containsEntry("kty", "RSA").containsKeys("kid", "n", "e")
                .doesNotContainKeys("d", "p", "q", "dp", "dq", "qi");
    }
}
