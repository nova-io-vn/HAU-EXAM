package com.userservice.infrastructure.persistence.adapter;

import com.userservice.domain.model.UserOnboardingState;
import com.userservice.domain.model.Role;
import com.userservice.domain.repository.UserOnboardingRepository;
import com.userservice.infrastructure.persistence.entity.UserOnboardingStateEntity;
import com.userservice.infrastructure.persistence.repository.JpaUserOnboardingRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserOnboardingPersistenceAdapter implements UserOnboardingRepository {
    private final JpaUserOnboardingRepository repository;
    public UserOnboardingPersistenceAdapter(JpaUserOnboardingRepository repository) { this.repository = repository; }
    @Override public Optional<UserOnboardingState> findByUserIdAndRole(UUID userId, Role role) {
        return repository.findByUserIdAndRole(userId, role).map(this::toDomain);
    }
    @Override public UserOnboardingState save(UserOnboardingState state) {
        UserOnboardingStateEntity entity = repository.findById(state.getId()).orElseGet(() -> new UserOnboardingStateEntity(state.getId(), state.getUserId(), state.getRole(), 0, null));
        entity.setVersionCompleted(state.getVersionCompleted()); entity.setCompletedAt(state.getCompletedAt());
        return toDomain(repository.save(entity));
    }
    private UserOnboardingState toDomain(UserOnboardingStateEntity entity) { return new UserOnboardingState(entity.getId(), entity.getUserId(), entity.getRole(), entity.getVersionCompleted(), entity.getCompletedAt()); }
}
