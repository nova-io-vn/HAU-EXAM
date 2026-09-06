package com.notificationservice.domain.repository;

import com.notificationservice.domain.model.DeviceToken;
import java.util.*;

public interface DeviceTokenRepository {
    DeviceToken save(DeviceToken token);
    Optional<DeviceToken> findByToken(String token);
    List<DeviceToken> findActiveByUser(UUID userId);
}
