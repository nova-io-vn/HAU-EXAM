package com.questionservice.domain.exception;

public class ForbiddenException extends DomainException {
    private final String code;
    public ForbiddenException(String message) { this("FORBIDDEN", message); }
    public ForbiddenException(String code, String message) { super(message); this.code = code; }
    public String code() { return code; }
}
