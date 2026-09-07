package com.userservice.application.service;

import com.userservice.domain.exception.UserNotFoundException;
import com.userservice.domain.model.Role;
import com.userservice.domain.model.UserOnboardingState;
import com.userservice.domain.model.UserProfile;
import com.userservice.domain.repository.UserOnboardingRepository;
import com.userservice.domain.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class UserOnboardingService {
    private final UserOnboardingRepository onboarding;
    private final UserProfileRepository profiles;
    private final Clock clock;
    public UserOnboardingService(UserOnboardingRepository onboarding, UserProfileRepository profiles, Clock clock) { this.onboarding = onboarding; this.profiles = profiles; this.clock = clock; }
    @Transactional(readOnly = true)
    public UserOnboardingState current(UUID userId) { return state(userId, profile(userId).getRole()); }
    @Transactional
    public UserOnboardingState complete(UUID userId, int version) { UserProfile profile = profile(userId); UserOnboardingState current = state(userId, profile.getRole()); return onboarding.save(current.complete(version, Instant.now(clock))); }
    private UserOnboardingState state(UUID userId, Role role) { return onboarding.findByUserIdAndRole(userId, role).orElseGet(() -> new UserOnboardingState(UUID.randomUUID(), userId, role, 0, null)); }
    private UserProfile profile(UUID id) { return profiles.findById(id).orElseThrow(() -> new UserNotFoundException(id)); }
}
