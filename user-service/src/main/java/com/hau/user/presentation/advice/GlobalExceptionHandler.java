package com.hau.user.presentation.advice;

import com.hau.user.domain.exception.*;
import com.hau.user.presentation.response.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log=LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> validation(MethodArgumentNotValidException ex,HttpServletRequest req){var errors=ex.getBindingResult().getFieldErrors().stream().map(e->new FieldValidationError(e.getField(),e.getDefaultMessage())).toList();return error(HttpStatus.BAD_REQUEST,"VALIDATION_ERROR","Request validation failed",errors,req);}
    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiErrorResponse> constraint(ConstraintViolationException ex,HttpServletRequest req){var errors=ex.getConstraintViolations().stream().map(e->new FieldValidationError(e.getPropertyPath().toString(),e.getMessage())).toList();return error(HttpStatus.BAD_REQUEST,"VALIDATION_ERROR","Request validation failed",errors,req);}
    @ExceptionHandler(DomainException.class)
    ResponseEntity<ApiErrorResponse> domain(DomainException ex,HttpServletRequest req){HttpStatus status=switch(ex){case UserNotFoundException ignored->HttpStatus.NOT_FOUND;case ForbiddenOperationException ignored->HttpStatus.FORBIDDEN;case DuplicateUserException ignored->HttpStatus.CONFLICT;default->HttpStatus.BAD_REQUEST;};return error(status,ex.getCode(),ex.getMessage(),List.of(),req);}
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiErrorResponse> invalid(IllegalArgumentException ex,HttpServletRequest req){return error(HttpStatus.BAD_REQUEST,"INVALID_REQUEST","Invalid request",List.of(),req);}
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> unexpected(Exception ex,HttpServletRequest req){log.error("Unexpected user service error",ex);return error(HttpStatus.INTERNAL_SERVER_ERROR,"INTERNAL_ERROR","An unexpected error occurred",List.of(),req);}
    private ResponseEntity<ApiErrorResponse> error(HttpStatus s,String code,String msg,List<FieldValidationError> errors,HttpServletRequest req){return ResponseEntity.status(s).body(ApiErrorResponse.of(code,msg,req.getRequestURI(),req.getHeader("X-Correlation-Id"),errors));}
}
