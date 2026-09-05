package com.hau.user.application.dto;

import com.hau.user.domain.model.Role;
import java.util.UUID;
public record ActorContext(UUID userId, Role role, String facultyId) { }
