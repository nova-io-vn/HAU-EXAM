package com.userservice.presentation.controller;

import com.userservice.application.dto.UserContact;
import com.userservice.application.port.in.UserContactQueryUseCase;
import com.userservice.infrastructure.security.InternalServiceTokenVerifier;
import com.userservice.presentation.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/users")
public class InternalUserContactController {
    private final UserContactQueryUseCase contacts;
    private final InternalServiceTokenVerifier tokens;

    public InternalUserContactController(UserContactQueryUseCase contacts, InternalServiceTokenVerifier tokens) {
        this.contacts = contacts;
        this.tokens = tokens;
    }

    @GetMapping("/{id}/contact")
    public ApiResponse<UserContact> contact(@RequestHeader(value = "X-Internal-Service-Token", required = false) String token, @PathVariable UUID id) {
        if (!tokens.matches(token)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid service token");
        return ApiResponse.success(contacts.find(id));
    }

    @GetMapping("/lecturers")
    public ApiResponse<List<UserContact>> lecturers(
            @RequestHeader(value = "X-Internal-Service-Token", required = false) String token,
            @RequestParam(required = false) Set<String> facultyId,
            @RequestParam(required = false) String keyword) {
        if (!tokens.matches(token)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid service token");
        return ApiResponse.success(contacts.findActiveLecturers(facultyId, keyword));
    }

    @GetMapping("/contacts")
    public ApiResponse<List<UserContact>> contacts(
            @RequestHeader(value = "X-Internal-Service-Token", required = false) String token,
            @RequestParam Set<UUID> ids) {
        if (!tokens.matches(token)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid service token");
        return ApiResponse.success(contacts.findAll(ids));
    }
}
