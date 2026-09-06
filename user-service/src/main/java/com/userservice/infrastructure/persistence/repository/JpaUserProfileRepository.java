package com.userservice.infrastructure.persistence.repository;

import com.userservice.infrastructure.persistence.entity.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
public interface JpaUserProfileRepository extends JpaRepository<UserProfileEntity,UUID> {
    Optional<UserProfileEntity> findByLecturerCode(String lecturerCode);
    boolean existsByLecturerCode(String lecturerCode);
    boolean existsByEmailIgnoreCase(String email);
}
