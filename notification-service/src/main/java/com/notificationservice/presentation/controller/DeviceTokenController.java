package com.notificationservice.presentation.controller;

import com.notificationservice.application.port.in.DeviceTokenUseCase;
import com.notificationservice.presentation.request.DeviceTokenRequest;
import com.notificationservice.presentation.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications/devices")
public class DeviceTokenController {
    private final DeviceTokenUseCase useCase;
    public DeviceTokenController(DeviceTokenUseCase useCase) { this.useCase = useCase; }

    @PostMapping
    public ApiResponse<Void> register(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody DeviceTokenRequest request) {
        useCase.register(userId(jwt), request.token(), request.platform(), request.deviceIdentifier());
        return ApiResponse.success(null);
    }

    @DeleteMapping
    public ApiResponse<Void> unregister(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody DeviceTokenRequest request) {
        useCase.unregister(userId(jwt), request.token());
        return ApiResponse.success(null);
    }

    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
