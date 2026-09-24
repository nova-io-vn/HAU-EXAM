package com.notificationservice.presentation.advice;

import com.notificationservice.application.exception.EmailDeliveryException;
import com.notificationservice.application.exception.EmailSettingsException;
import com.notificationservice.domain.exception.*;
import com.notificationservice.presentation.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.*;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(DomainException.class)
    ResponseEntity<ApiErrorResponse> domain(DomainException e, HttpServletRequest r) { HttpStatus s = e instanceof NotificationNotFoundException ? HttpStatus.NOT_FOUND : e instanceof ForbiddenNotificationAccessException ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST; return response(s, e.getCode(), e.getMessage(), r); }
    @ExceptionHandler(EmailSettingsException.class)
    ResponseEntity<ApiErrorResponse> settings(EmailSettingsException e, HttpServletRequest r) { return response(HttpStatus.BAD_REQUEST, e.getCode(), e.getMessage(), r); }
    @ExceptionHandler(EmailDeliveryException.class)
    ResponseEntity<ApiErrorResponse> email(EmailDeliveryException e, HttpServletRequest r) { String message = switch (e.getCode()) { case "EMAIL_DELIVERY_DISABLED" -> "Gửi email đang bị tắt."; case "SMTP_CONFIGURATION_INVALID" -> e.getMessage(); case "SMTP_CREDENTIAL_DECRYPTION_FAILED" -> "Không thể đọc thông tin xác thực SMTP đã lưu."; case "SMTP_CONNECTION_FAILED" -> "Không thể kết nối tới máy chủ SMTP. Hãy kiểm tra host, cổng và chế độ bảo mật."; case "SMTP_AUTHENTICATION_FAILED" -> "SMTP từ chối thông tin đăng nhập. Hãy kiểm tra tài khoản và mật khẩu ứng dụng."; case "SMTP_TLS_FAILED" -> "Không thể thiết lập kết nối TLS với máy chủ SMTP."; default -> "Không thể gửi email kiểm tra."; }; HttpStatus status = switch (e.getCode()) { case "EMAIL_DELIVERY_DISABLED", "SMTP_CONFIGURATION_INVALID", "SMTP_CREDENTIAL_DECRYPTION_FAILED" -> HttpStatus.BAD_REQUEST; default -> HttpStatus.BAD_GATEWAY; }; return response(status, e.getCode(), message, r); }
    @ExceptionHandler({MethodArgumentNotValidException.class, IllegalArgumentException.class})
    ResponseEntity<ApiErrorResponse> bad(Exception e, HttpServletRequest r) { return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Cấu hình hoặc yêu cầu chưa hợp lệ.", r); }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> unexpected(Exception e, HttpServletRequest r) { log.error("Unexpected notification service error", e); return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred", r); }
    private ResponseEntity<ApiErrorResponse> response(HttpStatus s, String c, String m, HttpServletRequest r) { return ResponseEntity.status(s).body(ApiErrorResponse.of(c, m, r.getRequestURI(), r.getHeader("X-Correlation-Id"))); }
}
