package com.aiservice.presentation.controller;

import com.aiservice.application.service.AiJobService;
import com.aiservice.application.service.AiWorkspaceService;
import com.aiservice.application.port.out.UserDirectoryPort;
import com.aiservice.domain.model.JobType;
import com.aiservice.presentation.request.AiRequests.*;
import com.aiservice.presentation.response.*;
import com.aiservice.presentation.response.AiResponses.JobView;
import jakarta.validation.Valid;

import java.util.UUID;

import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/v1")
public class AiController {
    private final AiJobService jobs;
    private final ObjectMapper mapper;
    private final AiWorkspaceService workspace;
    private final UserDirectoryPort users;

    public AiController(AiJobService j, ObjectMapper m, AiWorkspaceService workspace, UserDirectoryPort users) {
        jobs = j; mapper = m; this.workspace = workspace; this.users = users;
    }

    @PostMapping("/ai/generate/questions")
    public ResponseEntity<ApiResponse<JobView>> generate(@AuthenticationPrincipal Jwt jwt, @RequestHeader(value = "X-Correlation-Id", required = false) UUID c, @Valid @RequestBody GenerateRequest r) {
        return accepted(JobView.from(jobs.create(UUID.fromString(jwt.getSubject()), r.documentId(), JobType.QUESTION_GENERATION, mapper.writeValueAsString(r), c, jwt.getClaimAsString("facultyId"), r.subjectId(), r.chapterId(), r.topicId(), jwt.getClaimAsString("role"))));
    }

    @PostMapping("/ai/analyze")
    public ResponseEntity<ApiResponse<JobView>> analyze(@AuthenticationPrincipal Jwt jwt, @RequestHeader(value = "X-Correlation-Id", required = false) UUID c, @Valid @RequestBody AnalyzeRequest r) {
        return accepted(create(jwt, r.documentId(), JobType.ANALYSIS, r, c));
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<JobView>> chat(@AuthenticationPrincipal Jwt jwt, @RequestHeader(value = "X-Correlation-Id", required = false) UUID c, @Valid @RequestBody ChatRequest r) {
        return accepted(create(jwt, r.documentId(), JobType.CHAT, r, c));
    }

    @GetMapping("/ai/jobs/{id}")
    public ApiResponse<JobView> get(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(JobView.from(jobs.get(id, UUID.fromString(jwt.getSubject()))));
    }

    @GetMapping("/admin/ai/jobs")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ApiResponse<?> adminJobs(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
        var result = workspace.allJobs(page, size);
        return ApiResponse.ok(new com.aiservice.application.model.WorkspacePage<>(result.items().stream().map(job -> JobView.from(job, users.displayName(job.requestedBy()))).toList(), result.page(), result.size(), result.totalElements(), result.totalPages()));
    }

    private JobView create(Jwt jwt, UUID doc, JobType type, Object body, UUID c) {
        return JobView.from(jobs.create(UUID.fromString(jwt.getSubject()), doc, type, mapper.writeValueAsString(body), c));
    }

    private ResponseEntity<ApiResponse<JobView>> accepted(JobView j) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ApiResponse<>(true, "AI_JOB_ACCEPTED", "AI job accepted", j));
    }
}
