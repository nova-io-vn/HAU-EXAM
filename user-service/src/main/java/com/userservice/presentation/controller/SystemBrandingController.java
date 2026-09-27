package com.userservice.presentation.controller;

import com.userservice.application.model.ImageUploadCommand;
import com.userservice.application.model.SystemBranding;
import com.userservice.application.service.SystemBrandingService;
import com.userservice.presentation.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class SystemBrandingController {
    private final SystemBrandingService branding;

    public SystemBrandingController(SystemBrandingService branding) { this.branding = branding; }

    @GetMapping("/api/v1/public/system-branding")
    public ApiResponse<PublicBranding> publicBranding() { return ApiResponse.success(PublicBranding.from(branding.current())); }

    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @GetMapping("/api/v1/admin/platform/branding")
    public ApiResponse<PublicBranding> adminBranding() { return ApiResponse.success(PublicBranding.from(branding.current())); }

    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @PutMapping("/api/v1/admin/platform/branding")
    public ApiResponse<PublicBranding> update(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody BrandingRequest request) {
        return ApiResponse.success(PublicBranding.from(branding.update(request.systemName(), request.shortName(), actor(jwt))));
    }

    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @PostMapping(value = "/api/v1/admin/platform/branding/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PublicBranding> logo(@AuthenticationPrincipal Jwt jwt, @RequestPart("file") MultipartFile file) throws IOException {
        return ApiResponse.success(PublicBranding.from(branding.uploadLogo(command(file), actor(jwt))));
    }

    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @PostMapping(value = "/api/v1/admin/platform/branding/favicon", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PublicBranding> favicon(@AuthenticationPrincipal Jwt jwt, @RequestPart("file") MultipartFile file) throws IOException {
        return ApiResponse.success(PublicBranding.from(branding.uploadFavicon(command(file), actor(jwt))));
    }

    private ImageUploadCommand command(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Image file is required");
        return new ImageUploadCommand(file.getBytes(), file.getOriginalFilename(), file.getContentType(), file.getSize());
    }
    private UUID actor(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }

    public record BrandingRequest(@NotBlank @Size(max = 120) String systemName,
                                  @NotBlank @Size(max = 40) String shortName) { }
    public record PublicBranding(String systemName, String shortName, String logoUrl, String faviconUrl) {
        static PublicBranding from(SystemBranding value) {
            return new PublicBranding(value.systemName(), value.shortName(), value.logoUrl(), value.faviconUrl());
        }
    }
}
