package com.userservice.application.dto;

import java.util.UUID;

public record AudienceMember(UUID userId, String email) { }
