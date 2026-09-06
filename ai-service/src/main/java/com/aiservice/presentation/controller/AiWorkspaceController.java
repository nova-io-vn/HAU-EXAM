package com.aiservice.presentation.controller;

import com.aiservice.application.model.WorkspacePage;
import com.aiservice.application.service.AiWorkspaceService;
import com.aiservice.infrastructure.security.InternalServiceTokenVerifier;
import com.aiservice.presentation.response.ApiResponse;
import com.aiservice.presentation.response.AiResponses.*;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/v1")
public class AiWorkspaceController {
    private final AiWorkspaceService workspace;
    private final ObjectMapper mapper;
    private final InternalServiceTokenVerifier internalServiceToken;

    public AiWorkspaceController(AiWorkspaceService workspace, ObjectMapper mapper, InternalServiceTokenVerifier internalServiceToken) {
        this.workspace = workspace;
        this.mapper = mapper;
        this.internalServiceToken = internalServiceToken;
    }

    @GetMapping("/documents")
    public ApiResponse<WorkspacePage<DocumentView>> documents(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        var result = workspace.documents(UUID.fromString(jwt.getSubject()), page, size);
        return ApiResponse.ok(new WorkspacePage<>(result.items().stream().map(DocumentView::from).toList(), result.page(), result.size(), result.totalElements(), result.totalPages()));
    }

    @GetMapping("/ai/jobs")
    public ApiResponse<WorkspacePage<JobView>> jobs(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        var result = workspace.jobs(UUID.fromString(jwt.getSubject()), page, size);
        return ApiResponse.ok(new WorkspacePage<>(result.items().stream().map(JobView::from).toList(), result.page(), result.size(), result.totalElements(), result.totalPages()));
    }

    @GetMapping("/ai/jobs/{id}/result")
    public ApiResponse<JsonNode> result(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(mapper.readTree(workspace.result(id, UUID.fromString(jwt.getSubject()))));
    }

    @GetMapping("/internal/ai/jobs/{id}/result")
    public ApiResponse<JsonNode> internalResult(@PathVariable UUID id, @RequestHeader(value = "X-Internal-Service-Token", required = false) String token) {
        if (!internalServiceToken.matches(token)) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid service token");
        }
        return ApiResponse.ok(mapper.readTree(workspace.resultInternal(id)));
    }
}
