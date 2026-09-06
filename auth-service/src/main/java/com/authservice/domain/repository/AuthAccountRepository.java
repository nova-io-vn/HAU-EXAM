package com.authservice.domain.repository;

import com.authservice.domain.model.AuthAccount;

import java.util.Optional;
import java.util.UUID;

public interface AuthAccountRepository {

    AuthAccount save(AuthAccount account);

    Optional<AuthAccount> findById(UUID id);

    Optional<AuthAccount> findByLecturerCode(String lecturerCode);

    boolean existsByLecturerCode(String lecturerCode);
}
