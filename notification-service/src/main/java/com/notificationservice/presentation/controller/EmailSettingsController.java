package com.notificationservice.presentation.controller;

import com.notificationservice.application.service.EmailSettingsService;
import com.notificationservice.presentation.request.EmailTestRequest;
import com.notificationservice.presentation.request.SmtpSettingsRequest;
import com.notificationservice.presentation.response.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/email-settings")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class EmailSettingsController {
    private final EmailSettingsService service;

    public EmailSettingsController(EmailSettingsService service) { this.service = service; }

    @GetMapping
    public ApiResponse<EmailSettingsResponse> get() {
        return ApiResponse.success(service.get());
    }

    @PutMapping
    public ApiResponse<EmailSettingsResponse> update(@Valid @RequestBody SmtpSettingsRequest request) { return ApiResponse.success(service.update(request)); }

    @PostMapping("/test")
    public ApiResponse<Void> test(@Valid @RequestBody EmailTestRequest request) {
        service.sendTest(request.recipient());
        return ApiResponse.success(null);
    }
}
