package com.notificationservice.application.exception;

public class EmailDeliveryException extends RuntimeException {
    private final String code;
    public EmailDeliveryException(String message, Throwable cause) { super(message, cause); this.code = "SMTP_SEND_FAILED"; }
    public EmailDeliveryException(String code, String message) { super(message); this.code = code; }
    public EmailDeliveryException(String code, String message, Throwable cause) { super(message, cause); this.code = code; }
    public String getCode() { return code == null ? "SMTP_SEND_FAILED" : code; }
}
