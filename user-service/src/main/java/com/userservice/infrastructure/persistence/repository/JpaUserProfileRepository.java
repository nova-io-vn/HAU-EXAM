package com.userservice.infrastructure.persistence.repository;

import com.userservice.infrastructure.persistence.entity.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import com.userservice.domain.model.Role;
import com.userservice.domain.model.UserStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
public interface JpaUserProfileRepository extends JpaRepository<UserProfileEntity,UUID>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<UserProfileEntity> {
    Optional<UserProfileEntity> findByLecturerCode(String lecturerCode);
    boolean existsByLecturerCode(String lecturerCode);
    boolean existsByEmailIgnoreCase(String email);
}
