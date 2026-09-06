package com.userservice.application.service;

import com.userservice.application.dto.ActorContext;
import com.userservice.application.port.in.UserAdministrationUseCase;
import com.userservice.application.port.out.UserEventPublisher;
import com.userservice.domain.exception.ForbiddenOperationException;
import com.userservice.domain.exception.UserNotFoundException;
import com.userservice.domain.model.Role;
import com.userservice.domain.model.UserProfile;
import com.userservice.domain.repository.PageQuery;
import com.userservice.domain.repository.PageResult;
import com.userservice.domain.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.function.UnaryOperator;

@Service
public class UserAdministrationService implements UserAdministrationUseCase {
    private final UserProfileRepository repository;
    private final UserEventPublisher publisher;
    private final Clock clock;

    public UserAdministrationService(UserProfileRepository repository, UserEventPublisher publisher, Clock clock) {
        this.repository = repository;
        this.publisher = publisher;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<UserProfile> list(ActorContext a, PageQuery q) {
        requireAdmin(a);
        return repository.findAll(q);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfile get(ActorContext a, UUID id) {
        requireAdmin(a);
        return find(id);
    }

    @Override
    @Transactional
    public UserProfile approve(ActorContext a, UUID id, UUID c) {
        return change(a, id, u -> u.approve(now()), u -> publisher.userApproved(u, c));
    }

    @Override
    @Transactional
    public UserProfile reject(ActorContext a, UUID id, UUID c) {
        return change(a, id, u -> u.reject(now()), u -> publisher.userRejected(u, c));
    }

    @Override
    @Transactional
    public UserProfile assignRole(ActorContext a, UUID id, Role role, UUID c) {
        return change(a, id, u -> u.assignRole(role, now()), u -> publisher.roleChanged(u, c));
    }

    @Override
    @Transactional
    public UserProfile assignFaculty(ActorContext a, UUID id, String faculty, UUID c) {
        return change(a, id, u -> u.assignFaculty(faculty, now()), u -> publisher.facultyChanged(u, c));
    }

    @Override
    @Transactional
    public UserProfile lock(ActorContext a, UUID id, UUID c) {
        return change(a, id, u -> u.lock(now()), u -> publisher.statusChanged(u, c));
    }

    @Override
    @Transactional
    public UserProfile unlock(ActorContext a, UUID id, UUID c) {
        return change(a, id, u -> u.unlock(now()), u -> publisher.statusChanged(u, c));
    }

    private UserProfile change(ActorContext actor, UUID id, UnaryOperator<UserProfile> operation, java.util.function.Consumer<UserProfile> event) {
        requireAdmin(actor);
        UserProfile saved = repository.save(operation.apply(find(id)));
        event.accept(saved);
        return saved;
    }

    private void requireAdmin(ActorContext actor) {
        if (actor == null || actor.role() != Role.SYSTEM_ADMIN)
            throw new ForbiddenOperationException("SYSTEM_ADMIN role is required");
    }

    private UserProfile find(UUID id) {
        return repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    private Instant now() {
        return Instant.now(clock);
    }
}
