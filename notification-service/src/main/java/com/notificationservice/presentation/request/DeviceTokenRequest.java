package com.notificationservice.presentation.request;

import com.notificationservice.domain.model.DevicePlatform;
import jakarta.validation.constraints.*;

public record DeviceTokenRequest(@NotBlank @Size(max = 512) String token,
                                 @NotNull DevicePlatform platform,
                                 @Size(max = 255) String deviceIdentifier) { }
