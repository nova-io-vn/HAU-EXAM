package com.userservice.application.service;

import com.userservice.application.dto.UpdateProfileCommand;
import com.userservice.application.port.in.UserProfileUseCase;
import com.userservice.domain.exception.DuplicateUserException;
import com.userservice.domain.exception.UserNotFoundException;
import com.userservice.domain.model.UserProfile;
import com.userservice.domain.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class UserProfileService implements UserProfileUseCase {
    private final UserProfileRepository repository;
    private final Clock clock;

    public UserProfileService(UserProfileRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfile getOwnProfile(UUID id) {
        return find(id);
    }

    @Override
    @Transactional
    public UserProfile updateOwnProfile(UUID id, UpdateProfileCommand c) {
        UserProfile current = find(id);
        if (!current.getEmail().equalsIgnoreCase(c.email()) && repository.existsByEmail(c.email()))
            throw new DuplicateUserException("Email is already in use");
        return repository.save(current.updateProfile(c.fullName(), c.dateOfBirth(), c.phone(), c.email(), c.address(), c.avatar(), Instant.now(clock)));
    }

    private UserProfile find(UUID id) {
        return repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }
}
