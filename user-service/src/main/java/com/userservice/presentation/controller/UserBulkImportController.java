package com.userservice.presentation.controller;

import com.userservice.application.dto.ActorContext;
import com.userservice.application.service.BulkLecturerImportService;
import com.userservice.domain.model.Role;
import com.userservice.presentation.response.ApiResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users/import")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class UserBulkImportController {
    private final BulkLecturerImportService service;
    public UserBulkImportController(BulkLecturerImportService service) { this.service = service; }

    @GetMapping("/template")
    public ResponseEntity<byte[]> template(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=hau-lecturer-import-template.xlsx")
                .body(service.template(actor(jwt)));
    }
    @PostMapping(value = "/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<BulkLecturerImportService.Preview> preview(@AuthenticationPrincipal Jwt jwt, @RequestPart("file") MultipartFile file) throws IOException {
        return ApiResponse.success(service.preview(actor(jwt), file.getOriginalFilename(), file.getContentType(), file.getSize(), file.getInputStream()));
    }
    @PostMapping(value = "/confirm", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<BulkLecturerImportService.ImportResult> confirm(@AuthenticationPrincipal Jwt jwt, @RequestPart("file") MultipartFile file,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlation) throws IOException {
        return ApiResponse.success(service.confirm(actor(jwt), file.getOriginalFilename(), file.getContentType(), file.getSize(), file.getInputStream(), correlation(correlation)));
    }
    private ActorContext actor(Jwt jwt) { return new ActorContext(UUID.fromString(jwt.getSubject()), Role.valueOf(jwt.getClaimAsString("role")), jwt.getClaimAsString("facultyId")); }
    private UUID correlation(String value) { try { return value == null ? UUID.randomUUID() : UUID.fromString(value); } catch (Exception ignored) { return UUID.randomUUID(); } }
}
