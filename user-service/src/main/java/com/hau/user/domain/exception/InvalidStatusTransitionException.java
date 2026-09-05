package com.hau.user.domain.exception;

public class InvalidStatusTransitionException extends DomainException {
    public InvalidStatusTransitionException(String message) { super("INVALID_STATUS_TRANSITION", message); }
}
