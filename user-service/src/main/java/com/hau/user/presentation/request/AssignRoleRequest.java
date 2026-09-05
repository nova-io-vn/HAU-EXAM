package com.hau.user.presentation.request;

import com.hau.user.domain.model.Role;
import jakarta.validation.constraints.NotNull;
public record AssignRoleRequest(@NotNull Role role) { }
