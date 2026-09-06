package com.userservice.domain.exception;

public class ForbiddenOperationException extends DomainException {
    public ForbiddenOperationException(String message) { super("FORBIDDEN", message); }
}
