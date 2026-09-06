package com.notificationservice.application.port.in;

import com.notificationservice.domain.model.*;
import java.util.UUID;

public interface DeviceTokenUseCase {
    DeviceToken register(UUID userId, String token, DevicePlatform platform, String deviceIdentifier);
    void unregister(UUID userId, String token);
}
