package com.authservice.domain.exception;

import java.util.UUID;

public final class AuthAccountNotFoundException extends DomainException {

    public AuthAccountNotFoundException(UUID accountId) {
        super("AUTH_ACCOUNT_NOT_FOUND", "Auth account not found: " + accountId);
    }
}
