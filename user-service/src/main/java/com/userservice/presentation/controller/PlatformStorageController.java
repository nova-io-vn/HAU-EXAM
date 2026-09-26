package com.userservice.presentation.controller;

import com.userservice.infrastructure.service.CloudinarySettingsService;
import com.userservice.infrastructure.service.VercelAnalyticsSettingsService;
import com.userservice.presentation.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/platform")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class PlatformStorageController {
    private final CloudinarySettingsService settings;
    private final VercelAnalyticsSettingsService vercelSettings;

    public PlatformStorageController(CloudinarySettingsService settings,VercelAnalyticsSettingsService vercelSettings) { this.settings = settings; this.vercelSettings = vercelSettings; }

    @GetMapping("/cloudinary")
    public ApiResponse<CloudinaryStatus> cloudinary() {
        var value = settings.current();
        return ApiResponse.success(new CloudinaryStatus(value.configured(), value.cloudName(), value.apiKeyConfigured(), value.apiSecretConfigured(), value.source()));
    }

    @PutMapping("/cloudinary")
    public ApiResponse<CloudinaryStatus> save(@Valid @RequestBody CloudinaryRequest request, @AuthenticationPrincipal Jwt jwt) {
        var value = settings.save(request.cloudName(), request.apiKey(), request.apiSecret(), java.util.UUID.fromString(jwt.getSubject()));
        return ApiResponse.success(new CloudinaryStatus(value.configured(), value.cloudName(), true, true, value.source()));
    }

    @GetMapping("/vercel-analytics")
    public ApiResponse<VercelStatus> vercel() {
        var value=vercelSettings.current();
        return ApiResponse.success(new VercelStatus(value.configured(),value.projectId(),value.teamId(),value.tokenConfigured(),value.source()));
    }

    @PutMapping("/vercel-analytics")
    public ApiResponse<VercelStatus> saveVercel(@Valid @RequestBody VercelRequest request,@AuthenticationPrincipal Jwt jwt) {
        var value=vercelSettings.save(request.projectId(),request.teamId(),request.token(),java.util.UUID.fromString(jwt.getSubject()));
        return ApiResponse.success(new VercelStatus(value.configured(),value.projectId(),value.teamId(),true,value.source()));
    }

    public record CloudinaryStatus(
            boolean configured,
            String cloudName,
            boolean apiKeyConfigured,
            boolean apiSecretConfigured,
            String source
    ) { }

    public record CloudinaryRequest(@NotBlank String cloudName, @NotBlank String apiKey, @NotBlank String apiSecret) { }
    public record VercelRequest(@NotBlank String projectId,@NotBlank String teamId,@NotBlank String token) { }
    public record VercelStatus(boolean configured,String projectId,String teamId,boolean tokenConfigured,String source) { }
}
