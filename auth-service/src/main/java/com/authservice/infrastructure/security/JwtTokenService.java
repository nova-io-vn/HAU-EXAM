package com.authservice.infrastructure.security;

import com.authservice.application.port.out.TokenService;
import com.authservice.domain.model.AuthAccount;
import com.authservice.infrastructure.config.JwtProperties;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.UUID;

@Component
public class JwtTokenService implements TokenService {
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final JwtProperties properties;
    public JwtTokenService(JwtEncoder encoder, JwtDecoder decoder, JwtProperties properties) { this.encoder = encoder; this.decoder = decoder; this.properties = properties; }
    public IssuedTokens issue(AuthAccount account) {
        Instant now = Instant.now(); Instant accessExp = now.plus(properties.getAccessTokenTtl()); Instant refreshExp = now.plus(properties.getRefreshTokenTtl()); UUID refreshId = UUID.randomUUID();
        String access = encode(account, now, accessExp, UUID.randomUUID(), "access");
        String refresh = encode(account, now, refreshExp, refreshId, "refresh");
        return new IssuedTokens(access, refresh, refreshId, accessExp, refreshExp);
    }
    private String encode(AuthAccount account, Instant now, Instant exp, UUID id, String type) {
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder().issuer(properties.getIssuer()).subject(account.getId().toString()).issuedAt(now).expiresAt(exp)
                .id(id.toString()).claim("lecturerCode", account.getLecturerCode()).claim("role", account.getRole()).claim("tokenType", type);
        if (account.getFacultyId() != null) claims.claim("facultyId", account.getFacultyId());
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(SignatureAlgorithm.RS256).keyId(properties.getKeyId()).build(), claims.build())).getTokenValue();
    }
    public RefreshClaims parseRefreshToken(String token) {
        try {
            Jwt jwt = decoder.decode(token);
            if (!"refresh".equals(jwt.getClaimAsString("tokenType"))) throw new JwtException("wrong token type");
            return new RefreshClaims(UUID.fromString(jwt.getSubject()), UUID.fromString(jwt.getId()), jwt.getExpiresAt());
        } catch (RuntimeException ex) { throw new TokenException(); }
    }
    public static class TokenException extends RuntimeException { public TokenException() { super("Invalid refresh token"); } }
}
