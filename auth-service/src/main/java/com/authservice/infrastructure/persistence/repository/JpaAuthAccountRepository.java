package com.authservice.infrastructure.persistence.repository;

import com.authservice.infrastructure.persistence.entity.AuthAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaAuthAccountRepository extends JpaRepository<AuthAccountEntity, UUID> {

    Optional<AuthAccountEntity> findByLecturerCode(String lecturerCode);

    boolean existsByLecturerCode(String lecturerCode);
}
