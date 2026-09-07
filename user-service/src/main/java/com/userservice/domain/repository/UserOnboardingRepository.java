package com.userservice.domain.repository;

import com.userservice.domain.model.Role;
import com.userservice.domain.model.UserOnboardingState;

import java.util.Optional;
import java.util.UUID;

public interface UserOnboardingRepository {
    Optional<UserOnboardingState> findByUserIdAndRole(UUID userId, Role role);
    UserOnboardingState save(UserOnboardingState state);
}
