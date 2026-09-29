package com.gateway.presence;

import java.time.Clock;
import java.time.Duration;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@Service
public class PresenceService {
    static final String KEY = "hau:presence:online-users";
    static final Duration TTL = Duration.ofSeconds(90);
    private final ReactiveStringRedisTemplate redis;
    private final Clock clock;

    @Autowired
    public PresenceService(ReactiveStringRedisTemplate redis) {
        this(redis, Clock.systemUTC());
    }

    PresenceService(ReactiveStringRedisTemplate redis, Clock clock) {
        this.redis = redis;
        this.clock = clock;
    }

    public Mono<Void> touch(String userId) {
        double expiresAt = clock.instant().plus(TTL).getEpochSecond();
        return redis.opsForZSet().add(KEY, userId, expiresAt).then();
    }

    public Mono<Long> onlineCount() {
        double now = clock.instant().getEpochSecond();
        return redis.opsForZSet().removeRangeByScore(KEY, Range.closed(0.0, now))
                .then(redis.opsForZSet().count(KEY, Range.unbounded()));
    }

    public Flux<String> onlineUserIds() {
        double now = clock.instant().getEpochSecond();
        return redis.opsForZSet().removeRangeByScore(KEY, Range.closed(0.0, now))
                .thenMany(redis.opsForZSet().range(KEY, Range.unbounded()));
    }
}
