package com.userservice.presentation.controller;

import com.userservice.application.dto.WebTrafficSummary;
import com.userservice.application.service.WebAnalyticsService;
import com.userservice.presentation.response.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/analytics")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class WebAnalyticsController {
    private final WebAnalyticsService service;

    public WebAnalyticsController(WebAnalyticsService service) {
        this.service = service;
    }

    @GetMapping("/traffic")
    public ApiResponse<WebTrafficSummary> traffic() {
        return ApiResponse.success(service.trafficSummary());
    }
}
