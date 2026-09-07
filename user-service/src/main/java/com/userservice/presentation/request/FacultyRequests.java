package com.userservice.presentation.request;
import jakarta.validation.constraints.*;
public final class FacultyRequests { private FacultyRequests(){} public record Save(@NotBlank @Size(max=50) String code,@NotBlank @Size(max=255) String name,@Size(max=1000) String description, Boolean active){} public record Status(@NotNull Boolean active){} }
