package com.authservice.application.port.out;

import java.time.Instant;

public interface OtpStore {
    void save(String identity, String otpHash, Instant expiresAt);
    Verification verify(String identity, String otp);
    void markVerified(String identity, Instant expiresAt);
    boolean isVerified(String identity);
    void invalidate(String identity);

    enum Verification { VALID, INVALID, EXPIRED, TOO_MANY_ATTEMPTS, NOT_FOUND }
}
