package com.hau.ai.presentation.controller;

import com.hau.ai.application.model.WorkspacePage;
import com.hau.ai.application.service.AiWorkspaceService;
import com.hau.ai.presentation.response.ApiResponse;
import com.hau.ai.presentation.response.AiResponses.*;
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

    public AiWorkspaceController(AiWorkspaceService workspace, ObjectMapper mapper) {
        this.workspace = workspace;
        this.mapper = mapper;
    }

    @GetMapping("/documents")
    public ApiResponse<WorkspacePage<DocumentView>> documents(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        var result = workspace.documents(UUID.fromString(jwt.getSubject()), page, size);
        return ApiResponse.ok(new WorkspacePage<>(result.items().stream().map(DocumentView::from).toList(),
                result.page(), result.size(), result.totalElements(), result.totalPages()));
    }

    @GetMapping("/ai/jobs")
    public ApiResponse<WorkspacePage<JobView>> jobs(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        var result = workspace.jobs(UUID.fromString(jwt.getSubject()), page, size);
        return ApiResponse.ok(new WorkspacePage<>(result.items().stream().map(JobView::from).toList(),
                result.page(), result.size(), result.totalElements(), result.totalPages()));
    }

    @GetMapping("/ai/jobs/{id}/result")
    public ApiResponse<JsonNode> result(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(mapper.readTree(workspace.result(id, UUID.fromString(jwt.getSubject()))));
    }
}
