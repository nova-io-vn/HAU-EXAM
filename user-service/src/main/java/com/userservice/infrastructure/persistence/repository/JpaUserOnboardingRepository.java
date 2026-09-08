package com.userservice.infrastructure.persistence.repository;

import com.userservice.domain.model.Role;
import com.userservice.infrastructure.persistence.entity.UserOnboardingStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface JpaUserOnboardingRepository extends JpaRepository<UserOnboardingStateEntity, UUID> {
    Optional<UserOnboardingStateEntity> findByUserIdAndRole(UUID userId, Role role);
}
