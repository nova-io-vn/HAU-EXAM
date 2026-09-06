package com.authservice.infrastructure.redis;

import com.authservice.application.port.out.OtpStore;
import com.authservice.application.port.out.PasswordHasher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Component
public class RedisOtpStore implements OtpStore {
    private static final int MAX_ATTEMPTS = 5;
    private final StringRedisTemplate redis;
    private final PasswordHasher hasher;
    public RedisOtpStore(StringRedisTemplate redis, PasswordHasher hasher) { this.redis = redis; this.hasher = hasher; }
    private String key(String identity) { return "auth:otp:PASSWORD_RESET:" + identity; }
    public void save(String identity, String otpHash, Instant expiresAt) {
        String key = key(identity); redis.opsForHash().putAll(key, Map.of("hash", otpHash, "attempts", "0", "verified", "false"));
        redis.expireAt(key, expiresAt);
    }
    public Verification verify(String identity, String otp) {
        String key = key(identity); Map<Object, Object> state = redis.opsForHash().entries(key);
        if (state.isEmpty()) return Verification.NOT_FOUND;
        Long ttl = redis.getExpire(key);
        if (ttl != null && ttl <= 0) return Verification.EXPIRED;
        int attempts = Integer.parseInt(String.valueOf(state.getOrDefault("attempts", "0")));
        if (attempts >= MAX_ATTEMPTS) return Verification.TOO_MANY_ATTEMPTS;
        redis.opsForHash().increment(key, "attempts", 1);
        return hasher.matches(otp, String.valueOf(state.get("hash"))) ? Verification.VALID : Verification.INVALID;
    }
    public void markVerified(String identity, Instant expiresAt) { redis.opsForHash().put(key(identity), "verified", "true"); redis.expireAt(key(identity), expiresAt); }
    public boolean isVerified(String identity) { return "true".equals(redis.opsForHash().get(key(identity), "verified")); }
    public void invalidate(String identity) { redis.delete(key(identity)); }
}
