package com.userservice.presentation.controller;

import com.userservice.application.dto.AudienceMember;
import com.userservice.application.port.in.AudienceQueryUseCase;
import com.userservice.infrastructure.security.InternalServiceTokenVerifier;
import com.userservice.presentation.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/internal/users")
public class InternalAudienceController {
    private final AudienceQueryUseCase audience;
    private final InternalServiceTokenVerifier tokens;
    public InternalAudienceController(AudienceQueryUseCase audience, InternalServiceTokenVerifier tokens) {
        this.audience = audience; this.tokens = tokens;
    }
    @GetMapping("/audience")
    public ApiResponse<List<AudienceMember>> audience(
            @RequestHeader(value = "X-Internal-Service-Token", required = false) String token,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String facultyId) {
        if (!tokens.matches(token)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid service token");
        return ApiResponse.success(audience.resolve(role, facultyId));
    }
}
