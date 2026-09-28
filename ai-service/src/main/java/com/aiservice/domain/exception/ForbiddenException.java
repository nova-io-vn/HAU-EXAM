package com.aiservice.domain.exception;
public class ForbiddenException extends RuntimeException {
    private final String code;
    public ForbiddenException(String code) { this(code, code); }
    public ForbiddenException(String code, String message) { super(message); this.code = code; }
    public String code() { return code; }
}
