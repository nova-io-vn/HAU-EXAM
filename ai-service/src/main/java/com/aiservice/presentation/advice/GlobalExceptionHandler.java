package com.aiservice.presentation.advice;

import com.aiservice.domain.exception.*;
import com.aiservice.presentation.response.ApiResponse;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ApiResponse<Void> notFound(Exception e) {
        return err("RESOURCE_NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler({UnsupportedDocumentException.class, InvalidAiOutputException.class, IllegalArgumentException.class, MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiResponse<Void> bad(Exception e) {
        return err("VALIDATION_ERROR", e instanceof MethodArgumentNotValidException ? "Request validation failed" : e.getMessage());
    }

    @ExceptionHandler(InvalidJobTransitionException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ApiResponse<Void> conflict(Exception e) {
        return err("INVALID_JOB_TRANSITION", e.getMessage());
    }

    @ExceptionHandler(ProviderException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    ApiResponse<Void> provider(ProviderException e) {
        log.warn("AI provider request failed; retryable={} errorType={}", e.retryable(), e.getClass().getSimpleName());
        return err("AI_PROVIDER_UNAVAILABLE", "AI provider is temporarily unavailable");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ApiResponse<Void> unknown(Exception e) {
        log.error("Unexpected AI service error; errorType={}", e.getClass().getSimpleName(), e);
        return err("INTERNAL_ERROR", "Unexpected server error");
    }

    private ApiResponse<Void> err(String c, String m) {
        return new ApiResponse<>(false, c, m, null);
    }
}
