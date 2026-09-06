package com.authservice.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public final class AuthRequests {
    private AuthRequests() { }
    public record Register(@NotBlank @Size(max = 50) String lecturerCode,
                           @NotBlank @Size(min = 8, max = 100) String password,
                           @NotBlank @Size(max = 150) String fullName,
                           @Past LocalDate dateOfBirth,
                           @Size(max = 20) String phone,
                           @NotBlank @Email @Size(max = 254) String email,
                           @Size(max = 500) String address,
                           @Size(max = 1000) String avatar,
                           @Size(max = 50) String facultyId) { }
    public record Login(@NotBlank @Size(max = 50) String lecturerCode, @NotBlank String password) { }
    public record Refresh(@NotBlank String refreshToken) { }
    public record Logout(@NotBlank String refreshToken) { }
    public record ForgotPassword(@NotBlank @Size(max = 50) String lecturerCode) { }
    public record VerifyOtp(@NotBlank @Size(max = 50) String lecturerCode, @NotBlank @Size(min = 6, max = 6) String otp) { }
    public record ResetPassword(@NotBlank @Size(max = 50) String lecturerCode, @NotBlank @Size(min = 6, max = 6) String otp, @NotBlank @Size(min = 8, max = 100) String newPassword) { }
}
