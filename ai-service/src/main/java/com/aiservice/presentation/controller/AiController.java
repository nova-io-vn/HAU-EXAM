package com.aiservice.presentation.controller;

import com.aiservice.application.service.AiJobService;
import com.aiservice.application.service.AiWorkspaceService;
import com.aiservice.application.service.AiJobQueryService;
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
    private final AiJobQueryService jobQueries;

    public AiController(AiJobService j, ObjectMapper m, AiWorkspaceService workspace, UserDirectoryPort users, AiJobQueryService jobQueries) {
        jobs = j; mapper = m; this.workspace = workspace; this.users = users; this.jobQueries = jobQueries;
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
    public ApiResponse<?> get(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(detailView(jobQueries.detail(id, UUID.fromString(jwt.getSubject()), jwt.getClaimAsString("role"), jwt.getClaimAsString("facultyId"))));
    }

    @GetMapping("/admin/ai/jobs")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ApiResponse<?> adminJobs(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
        return ApiResponse.ok(jobQueries.all(page, size));
    }

    @PostMapping("/admin/ai/jobs/{id}/cancel")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ApiResponse<JobView> cancel(@PathVariable UUID id) {
        return ApiResponse.ok(JobView.from(jobs.cancel(id)));
    }

    @PostMapping("/admin/ai/jobs/{id}/retry")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<JobView>> retry(@PathVariable UUID id,
            @RequestHeader(value = "X-Correlation-Id", required = false) UUID correlationId) {
        return accepted(JobView.from(jobs.retry(id, correlationId)));
    }

    private Object detailView(com.aiservice.application.model.AiJobViews.Detail detail) {
        String result = detail.resultJson();
        return new JobDetailView(detail.summary(), detail.sourceType(), detail.document(), detail.description(), detail.generationConfig(),
                detail.provider(), detail.model(), detail.acceptedCount(), detail.rejectedCount(),
                result == null ? null : mapper.readTree(result), detail.errorCode(), detail.errorMessage());
    }

    public record JobDetailView(com.aiservice.application.model.AiJobViews.Summary summary, String sourceType,
            com.aiservice.application.model.AiJobViews.DocumentContext document, String description, java.util.Map<String,Object> generationConfig,
            String provider, String model, Integer acceptedCount, Integer rejectedCount, tools.jackson.databind.JsonNode result,
            String errorCode, String errorMessage) { }

    private JobView create(Jwt jwt, UUID doc, JobType type, Object body, UUID c) {
        return JobView.from(jobs.create(UUID.fromString(jwt.getSubject()), doc, type, mapper.writeValueAsString(body), c));
    }

    private ResponseEntity<ApiResponse<JobView>> accepted(JobView j) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ApiResponse<>(true, "AI_JOB_ACCEPTED", "AI job accepted", j));
    }
}
