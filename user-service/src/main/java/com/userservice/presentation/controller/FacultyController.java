package com.userservice.presentation.controller;

import com.userservice.application.dto.ActorContext;
import com.userservice.application.service.FacultyService;
import com.userservice.application.port.in.UserAdministrationUseCase;
import com.userservice.domain.model.*;
import com.userservice.domain.repository.*;
import com.userservice.presentation.request.FacultyRequests.*;
import com.userservice.presentation.response.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/faculties")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class FacultyController {
    private final FacultyService service;
    private final UserAdministrationUseCase users;

    public FacultyController(FacultyService s, UserAdministrationUseCase users) {
        service = s; this.users = users;
    }

    @GetMapping
    public ApiResponse<PageResponse<FacultyResponse>> list(@AuthenticationPrincipal Jwt j, @RequestParam(required = false) String keyword, @RequestParam(required = false) Boolean active, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        var p = service.search(actor(j), new FacultyQuery(keyword, active, page, size));
        return ApiResponse.success(new PageResponse<>(p.content().stream().map(service::response).toList(), p.page(), p.size(), p.totalElements(), p.totalPages()));
    }

    @GetMapping("/{id}")
    public ApiResponse<FacultyResponse> get(@AuthenticationPrincipal Jwt j, @PathVariable UUID id) {
        return ApiResponse.success(service.response(service.get(actor(j), id)));
    }

    @PostMapping
    public ApiResponse<FacultyResponse> create(@AuthenticationPrincipal Jwt j, @Valid @RequestBody Save r) {
        return ApiResponse.success(service.response(service.save(actor(j), null, r.code(), r.name(), r.description(), r.active() == null || r.active())));
    }

    @PutMapping("/{id}")
    public ApiResponse<FacultyResponse> update(@AuthenticationPrincipal Jwt j, @PathVariable UUID id, @Valid @RequestBody Save r) {
        return ApiResponse.success(service.response(service.save(actor(j), id, r.code(), r.name(), r.description(), r.active() == null || r.active())));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<FacultyResponse> status(@AuthenticationPrincipal Jwt j, @PathVariable UUID id, @Valid @RequestBody Status r) {
        return ApiResponse.success(service.response(service.status(actor(j), id, r.active())));
    }

    @PatchMapping("/{id}/subject-admin")
    public ApiResponse<FacultyResponse> subjectAdmin(@AuthenticationPrincipal Jwt j, @PathVariable UUID id, @Valid @RequestBody SubjectAdminRequest r, @RequestHeader(value="X-Correlation-Id", required=false) String c) {
        var faculty = service.get(actor(j), id);
        users.assignSubjectAdmin(actor(j), r.userId(), faculty.code(), correlation(c));
        return ApiResponse.success(service.response(faculty));
    }

    public record SubjectAdminRequest(UUID userId) {}

    private ActorContext actor(Jwt j) {
        return new ActorContext(UUID.fromString(j.getSubject()), Role.valueOf(j.getClaimAsString("role")), j.getClaimAsString("facultyId"));
    }
    private UUID correlation(String value) { try { return value == null ? UUID.randomUUID() : UUID.fromString(value); } catch (IllegalArgumentException e) { return UUID.randomUUID(); } }
}
