package com.authservice.infrastructure.security;

import com.authservice.application.port.out.RefreshTokenHasher;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class Sha256RefreshTokenHasher implements RefreshTokenHasher {

    @Override
    public String hash(CharSequence rawToken) {
        return HexFormat.of().formatHex(digest(rawToken));
    }

    @Override
    public boolean matches(CharSequence rawToken, String tokenHash) {
        if (tokenHash == null) return false;
        return MessageDigest.isEqual(digest(rawToken), decodeHex(tokenHash));
    }

    private byte[] digest(CharSequence rawToken) {
        if (rawToken == null) throw new IllegalArgumentException("rawToken must not be null");
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.toString().getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private byte[] decodeHex(String value) {
        try {
            return HexFormat.of().parseHex(value);
        } catch (IllegalArgumentException ex) {
            return new byte[0];
        }
    }
}
