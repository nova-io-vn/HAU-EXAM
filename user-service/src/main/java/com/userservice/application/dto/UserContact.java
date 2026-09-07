package com.userservice.application.dto;

import java.util.UUID;

public record UserContact(UUID userId, String lecturerCode, String fullName, String email, String facultyId) { }
