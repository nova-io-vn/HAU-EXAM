package com.questionservice.domain.exception;

public class NotFoundException extends DomainException {
    private final String code;
    public NotFoundException(String message) { this("RESOURCE_NOT_FOUND", message); }
    public NotFoundException(String code, String message) { super(message); this.code = code; }
    public String code() { return code; }
}
