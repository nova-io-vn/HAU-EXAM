package com.notificationservice.application.exception;

public class EmailSettingsException extends RuntimeException {
    private final String code;
    public EmailSettingsException(String code, String message) { super(message); this.code = code; }
    public EmailSettingsException(String code, String message, Throwable cause) { super(message, cause); this.code = code; }
    public String getCode() { return code; }
}
