package com.aiservice.presentation.controller;

import com.aiservice.application.service.SystemHelpService;
import com.aiservice.presentation.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/system-help")
public class SystemHelpController {
    private final SystemHelpService service;
    public SystemHelpController(SystemHelpService service) { this.service = service; }

    @PostMapping
    public ApiResponse<SystemHelpService.Result> ask(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody Request request) {
        return ApiResponse.ok(service.ask(jwt.getClaimAsString("role"), request.message().trim()));
    }

    public record Request(@NotBlank @Size(max = 1000) String message) {}
}
