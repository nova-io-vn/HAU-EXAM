package com.userservice.domain.exception;

public class InvalidUserProfileException extends DomainException {
    public InvalidUserProfileException(String message) { super("INVALID_USER_PROFILE", message); }
}
