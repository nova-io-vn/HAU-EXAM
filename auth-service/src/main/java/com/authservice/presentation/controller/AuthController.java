package com.authservice.presentation.controller;

import com.authservice.application.dto.AuthDtos;
import com.authservice.application.service.AuthApplicationService;
import com.authservice.presentation.request.AuthRequests;
import com.authservice.presentation.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthApplicationService auth;

    public AuthController(AuthApplicationService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthDtos.Registration>> register(@Valid @RequestBody AuthRequests.Register request, @RequestHeader(value = "X-Correlation-Id", required = false) UUID correlationId) {
        var input = new AuthDtos.RegistrationInput(request.lecturerCode(), request.password(), request.fullName(), request.dateOfBirth(), request.phone(), request.email(), request.address(), request.avatar(), request.facultyId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("REGISTERED", "Registration submitted", auth.register(input, correlationId)));
    }

    @PostMapping("/login")
    public ApiResponse<AuthDtos.Session> login(@Valid @RequestBody AuthRequests.Login request) {
        return ApiResponse.success("LOGIN_SUCCESS", "Login successful", auth.login(request.lecturerCode(), request.password()));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthDtos.Session> refresh(@Valid @RequestBody AuthRequests.Refresh request) {
        return ApiResponse.success("TOKEN_REFRESHED", "Access token refreshed", auth.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody AuthRequests.Logout request) {
        auth.logout(request.refreshToken());
        return ApiResponse.success("LOGOUT_SUCCESS", "Logout successful", null);
    }

    @PostMapping("/forgot-password")
    public ApiResponse<AuthDtos.Accepted> forgot(@Valid @RequestBody AuthRequests.ForgotPassword request, @RequestHeader(value = "X-Correlation-Id", required = false) UUID correlationId) {
        return ApiResponse.success("OTP_REQUESTED", "If the account exists, an OTP has been requested", auth.forgotPassword(request.lecturerCode(), correlationId));
    }

    @PostMapping("/verify-otp")
    public ApiResponse<AuthDtos.OtpVerification> verify(@Valid @RequestBody AuthRequests.VerifyOtp request) {
        return ApiResponse.success("OTP_VERIFIED", "OTP verified", auth.verifyOtp(request.lecturerCode(), request.otp()));
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> reset(@Valid @RequestBody AuthRequests.ResetPassword request) {
        auth.resetPassword(request.lecturerCode(), request.otp(), request.newPassword());
        return ApiResponse.success("PASSWORD_RESET", "Password reset successfully", null);
    }
}
