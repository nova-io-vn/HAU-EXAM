package com.userservice.domain.exception;

import java.util.UUID;
public class UserNotFoundException extends DomainException {
    public UserNotFoundException(UUID id) { super("USER_NOT_FOUND", "User not found: " + id); }
}
