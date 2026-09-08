package com.userservice.presentation.controller;

import com.userservice.application.service.FacultyService;
import com.userservice.domain.repository.FacultyQuery;
import com.userservice.presentation.response.ApiResponse;
import com.userservice.presentation.response.FacultyResponse;
import com.userservice.presentation.response.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/faculties")
public class PublicFacultyController {
    private final FacultyService service;
    public PublicFacultyController(FacultyService service) { this.service = service; }

    @GetMapping
    public ApiResponse<PageResponse<FacultyResponse>> list(@RequestParam(required = false) String keyword,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "100") int size) {
        var result = service.publicActive(new FacultyQuery(keyword, true, page, size));
        return ApiResponse.success(new PageResponse<>(result.content().stream().map(FacultyResponse::from).toList(), result.page(), result.size(), result.totalElements(), result.totalPages()));
    }
}
