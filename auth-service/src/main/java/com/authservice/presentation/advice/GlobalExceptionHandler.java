package com.authservice.presentation.advice;

import com.authservice.domain.exception.AuthAccountNotFoundException;
import com.authservice.domain.exception.DomainException;
import com.authservice.domain.exception.LecturerCodeAlreadyExistsException;
import com.authservice.application.service.AuthApplicationService.AuthException;
import com.authservice.infrastructure.security.JwtTokenService.TokenException;
import com.authservice.presentation.response.ApiErrorResponse;
import com.authservice.presentation.response.FieldValidationError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<FieldValidationError> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldValidationError(error.getField(), error.getDefaultMessage()))
                .toList();
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", errors, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        List<FieldValidationError> errors = exception.getConstraintViolations().stream()
                .map(violation -> new FieldValidationError(
                        violation.getPropertyPath().toString(), violation.getMessage()))
                .toList();
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", errors, request);
    }

    @ExceptionHandler(DomainException.class)
    ResponseEntity<ApiErrorResponse> handleDomainException(DomainException exception, HttpServletRequest request) {
        HttpStatus status = switch (exception) {
            case AuthAccountNotFoundException ignored -> HttpStatus.NOT_FOUND;
            case LecturerCodeAlreadyExistsException ignored -> HttpStatus.CONFLICT;
            case AuthException authException -> switch (authException.getCode()) {
                case "ACCOUNT_PENDING_APPROVAL", "ACCOUNT_REJECTED", "ACCOUNT_LOCKED" -> HttpStatus.FORBIDDEN;
                case "INVALID_CREDENTIALS", "INVALID_REFRESH_TOKEN", "INVALID_RESET_AUTHORIZATION", "OTP_EXPIRED", "INVALID_OTP" -> HttpStatus.UNAUTHORIZED;
                default -> HttpStatus.BAD_REQUEST;
            };
            default -> HttpStatus.BAD_REQUEST;
        };
        return error(status, exception.getCode(), exception.getMessage(), List.of(), request);
    }

    @ExceptionHandler(TokenException.class)
    ResponseEntity<ApiErrorResponse> handleToken(TokenException exception, HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "Refresh token is invalid", List.of(), request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception exception, HttpServletRequest request) {
        log.error("Unexpected authentication service error", exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "An unexpected error occurred", List.of(), request);
    }

    private ResponseEntity<ApiErrorResponse> error(
            HttpStatus status,
            String code,
            String message,
            List<FieldValidationError> errors,
            HttpServletRequest request
    ) {
        ApiErrorResponse body = ApiErrorResponse.of(
                code,
                message,
                request.getRequestURI(),
                request.getHeader(CORRELATION_ID_HEADER),
                errors
        );
        return ResponseEntity.status(status).body(body);
    }
}
