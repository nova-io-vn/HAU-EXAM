package com.authservice.application.port.out;

public interface RefreshTokenHasher {

    String hash(CharSequence rawToken);

    boolean matches(CharSequence rawToken, String tokenHash);
}
