package com.authservice.application.dto;

import com.authservice.domain.model.AccountStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class AuthDtos {
    private AuthDtos() { }

    public record Session(UUID userId, String lecturerCode, String role, String facultyId,
                          String accessToken, String refreshToken, Instant accessTokenExpiresAt,
                          Instant refreshTokenExpiresAt) { }
    public record Registration(AccountStatus status) { }
    public record RegistrationInput(String lecturerCode, String password, String fullName,
                                    LocalDate dateOfBirth, String phone, String email,
                                    String address, String avatar, String facultyId) { }
    public record OtpVerification(boolean verified, String resetToken) { }
    public record Accepted(String status) { }
}
