package com.hau.user.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record AssignFacultyRequest(@NotBlank @Size(max=50) String facultyId) { }
