package com.aiservice.presentation.controller;

import com.aiservice.application.service.DocumentService;
import com.aiservice.presentation.response.*;
import com.aiservice.presentation.response.AiResponses.DocumentView;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {
    private final DocumentService service;

    public DocumentController(DocumentService s) {
        service = s;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DocumentView> upload(@AuthenticationPrincipal Jwt jwt, @RequestPart("file") MultipartFile file) throws IOException {
        return ApiResponse.ok(DocumentView.from(service.upload(UUID.fromString(jwt.getSubject()), file.getOriginalFilename(), file.getContentType(), file.getSize(), file.getInputStream())));
    }

    @GetMapping("/{id}")
    public ApiResponse<DocumentView> get(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(DocumentView.from(service.get(id, UUID.fromString(jwt.getSubject()))));
    }
}
