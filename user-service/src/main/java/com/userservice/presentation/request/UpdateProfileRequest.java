package com.userservice.presentation.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record UpdateProfileRequest(
        @NotBlank @Size(max=150) String fullName,
        @Past LocalDate dateOfBirth,
        @Pattern(regexp="^$|^[0-9+() .-]{7,20}$") String phone,
        @NotBlank @Email @Size(max=254) String email,
        @Size(max=500) String address,
        @Size(max=1000) String avatar) { }
