package com.questionservice.presentation.controller;

import com.questionservice.application.service.CoverageService;
import com.questionservice.presentation.response.ApiResponse;
import com.questionservice.presentation.support.ActorResolver;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/coverage")
public class CoverageController {
    private final CoverageService service;private final ActorResolver actors;
    public CoverageController(CoverageService service,ActorResolver actors){this.service=service;this.actors=actors;}
    @GetMapping("/subjects/{subjectId}") @PreAuthorize("hasRole('SUBJECT_ADMIN')")
    public ApiResponse<CoverageService.SubjectCoverage> subject(@PathVariable UUID subjectId,@AuthenticationPrincipal Jwt jwt){return ApiResponse.ok(service.calculate(subjectId,actors.from(jwt)));}
}
