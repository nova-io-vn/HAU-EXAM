package com.userservice.presentation.controller;

import com.userservice.application.service.UserOnboardingService;
import com.userservice.domain.model.UserOnboardingState;
import com.userservice.presentation.request.CompleteOnboardingRequest;
import com.userservice.presentation.response.ApiResponse;
import com.userservice.presentation.response.OnboardingStateResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/me/onboarding")
public class UserOnboardingController {
    private final UserOnboardingService service;
    public UserOnboardingController(UserOnboardingService service) { this.service = service; }
    @GetMapping public ApiResponse<OnboardingStateResponse> current(@AuthenticationPrincipal Jwt jwt) { return ApiResponse.success(response(service.current(userId(jwt)))); }
    @PostMapping("/complete") public ApiResponse<OnboardingStateResponse> complete(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CompleteOnboardingRequest request) { return ApiResponse.success(response(service.complete(userId(jwt), request.version()))); }
    private OnboardingStateResponse response(UserOnboardingState state) { return new OnboardingStateResponse(state.getRole(), state.getVersionCompleted(), state.getCompletedAt()); }
    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
