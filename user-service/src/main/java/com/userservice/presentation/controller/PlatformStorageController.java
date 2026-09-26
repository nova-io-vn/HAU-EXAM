package com.userservice.presentation.controller;

import com.userservice.infrastructure.config.CloudinaryProperties;
import com.userservice.presentation.response.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/platform")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class PlatformStorageController {
    private final CloudinaryProperties cloudinary;

    public PlatformStorageController(CloudinaryProperties cloudinary) {
        this.cloudinary = cloudinary;
    }

    @GetMapping("/cloudinary")
    public ApiResponse<CloudinaryStatus> cloudinary() {
        return ApiResponse.success(new CloudinaryStatus(
                cloudinary.configured(),
                cloudinary.cloudName(),
                present(cloudinary.apiKey()),
                present(cloudinary.apiSecret())
        ));
    }

    private boolean present(String value) {
        return value != null && !value.isBlank();
    }

    public record CloudinaryStatus(
            boolean configured,
            String cloudName,
            boolean apiKeyConfigured,
            boolean apiSecretConfigured
    ) { }
}
