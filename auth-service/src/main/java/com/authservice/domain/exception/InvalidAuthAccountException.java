package com.authservice.domain.exception;

public final class InvalidAuthAccountException extends DomainException {

    public InvalidAuthAccountException(String message) {
        super("INVALID_AUTH_ACCOUNT", message);
    }
}
