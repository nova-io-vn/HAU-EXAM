package com.notificationservice.application.service;

import com.notificationservice.application.port.in.DeviceTokenUseCase;
import com.notificationservice.domain.model.*;
import com.notificationservice.domain.repository.DeviceTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class DeviceTokenService implements DeviceTokenUseCase {
    private final DeviceTokenRepository repository;
    private final Clock clock;

    public DeviceTokenService(DeviceTokenRepository repository, Clock clock) { this.repository = repository; this.clock = clock; }

    @Override @Transactional
    public DeviceToken register(UUID userId, String token, DevicePlatform platform, String deviceIdentifier) {
        var now = Instant.now(clock);
        return repository.findByToken(token).map(existing -> repository.save(existing.activateFor(userId, platform, deviceIdentifier, now)))
                .orElseGet(() -> repository.save(DeviceToken.register(userId, token, platform, deviceIdentifier, now)));
    }

    @Override @Transactional
    public void unregister(UUID userId, String token) {
        repository.findByToken(token).filter(existing -> existing.userId().equals(userId)).ifPresent(existing -> repository.save(existing.deactivate(Instant.now(clock))));
    }
}
