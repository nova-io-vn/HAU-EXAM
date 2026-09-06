package com.authservice.application.port.out;

public interface PasswordHasher {

    String hash(CharSequence rawPassword);

    boolean matches(CharSequence rawPassword, String passwordHash);
}
