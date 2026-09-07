package com.userservice.presentation.request;

import com.userservice.domain.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApproveUserRequest(@NotBlank @Size(max = 50) String facultyId, @NotNull Role role) {
}
