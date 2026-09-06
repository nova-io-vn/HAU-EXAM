package com.authservice.domain.exception;

public final class LecturerCodeAlreadyExistsException extends DomainException {

    public LecturerCodeAlreadyExistsException() {
        super("LECTURER_CODE_ALREADY_EXISTS", "Lecturer code already exists");
    }
}
