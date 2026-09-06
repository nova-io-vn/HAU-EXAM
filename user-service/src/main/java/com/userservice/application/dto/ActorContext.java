package com.userservice.application.dto;

import com.userservice.domain.model.Role;
import java.util.UUID;
public record ActorContext(UUID userId, Role role, String facultyId) { }
