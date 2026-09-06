package com.authservice.application.port.out;

import com.authservice.domain.model.AuthAccount;

import java.time.Instant;
import java.util.UUID;

public interface TokenService {
    IssuedTokens issue(AuthAccount account);
    RefreshClaims parseRefreshToken(String token);

    record IssuedTokens(String accessToken, String refreshToken, UUID refreshTokenId,
                        Instant accessExpiresAt, Instant refreshExpiresAt) { }
    record RefreshClaims(UUID accountId, UUID tokenId, Instant expiresAt) { }
}
