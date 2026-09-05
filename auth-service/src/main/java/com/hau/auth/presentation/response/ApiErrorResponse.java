package com.hau.auth.presentation.response;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        boolean success,
        String code,
        String message,
        Object data,
        Instant timestamp,
        String path,
        String correlationId,
        List<FieldValidationError> errors
) {
    public static ApiErrorResponse of(
            String code,
            String message,
            String path,
            String correlationId,
            List<FieldValidationError> errors
    ) {
        return new ApiErrorResponse(false, code, message, null, Instant.now(), path, correlationId, errors);
    }
}
