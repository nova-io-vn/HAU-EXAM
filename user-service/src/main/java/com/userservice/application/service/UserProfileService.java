package com.userservice.application.service;

import com.userservice.application.dto.UpdateProfileCommand;
import com.userservice.application.model.StoredImage;
import com.userservice.application.port.in.UserProfileUseCase;
import com.userservice.domain.exception.DuplicateUserException;
import com.userservice.domain.exception.UserNotFoundException;
import com.userservice.domain.model.UserProfile;
import com.userservice.domain.repository.UserProfileRepository;
import com.userservice.application.port.out.ImageStoragePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class UserProfileService implements UserProfileUseCase {
    private final UserProfileRepository repository;
    private final ImageStoragePort imageStorage;
    private final Clock clock;

    public UserProfileService(UserProfileRepository repository, ImageStoragePort imageStorage, Clock clock) {
        this.repository = repository;
        this.imageStorage = imageStorage;
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
        return repository.save(current.updateProfile(c.fullName(), c.dateOfBirth(), c.phone(), c.email(), c.address(), c.avatar(), c.academicRank(), c.academicDegree(), current.getAvatarPublicId(), Instant.now(clock)));
    }

    @Override
    @Transactional
    public UserProfile updateOwnAvatar(UUID id, StoredImage image) {
        UserProfile current = find(id);
        String oldPublicId = current.getAvatarPublicId();
        UserProfile updated = repository.save(current.replaceAvatar(image.secureUrl() != null ? image.secureUrl() : image.url(), image.publicId(), Instant.now(clock)));
        Runnable cleanup = () -> imageStorage.delete(oldPublicId);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { cleanup.run(); }
            });
        } else {
            cleanup.run();
        }
        return updated;
    }

    private UserProfile find(UUID id) {
        return repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }
}
