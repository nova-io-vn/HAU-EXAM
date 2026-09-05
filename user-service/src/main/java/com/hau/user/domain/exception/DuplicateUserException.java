package com.hau.user.domain.exception;

public class DuplicateUserException extends DomainException {
    public DuplicateUserException(String message) { super("USER_ALREADY_EXISTS", message); }
}
