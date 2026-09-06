package com.userservice.presentation.request;

import com.userservice.domain.model.Role;
import jakarta.validation.constraints.NotNull;
public record AssignRoleRequest(@NotNull Role role) { }
