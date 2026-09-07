package com.userservice.application.service;

import com.userservice.application.dto.ActorContext;
import com.userservice.domain.exception.ForbiddenOperationException;
import com.userservice.domain.model.*;
import com.userservice.domain.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class FacultyService {
    private final FacultyRepository repo;
    private final Clock clock;

    public FacultyService(FacultyRepository repo, Clock clock) {
        this.repo = repo;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResult<Faculty> search(ActorContext a, FacultyQuery q) {
        admin(a);
        return repo.search(q);
    }

    @Transactional(readOnly = true)
    public Faculty get(ActorContext a, UUID id) {
        admin(a);
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Faculty not found"));
    }

    @Transactional
    public Faculty save(ActorContext a, UUID id, String code, String name, String description, boolean active) {
        admin(a);
        Instant now = Instant.now(clock);
        var old = id == null ? null : repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Faculty not found"));
        return repo.save(new Faculty(id == null ? UUID.randomUUID() : id, code, name, description, active, old == null ? now : old.createdAt(), now));
    }

    @Transactional
    public Faculty status(ActorContext a, UUID id, boolean active) {
        var old = get(a, id);
        return repo.save(new Faculty(old.id(), old.code(), old.name(), old.description(), active, old.createdAt(), Instant.now(clock)));
    }

    private void admin(ActorContext a) {
        if (a == null || a.role() != Role.SYSTEM_ADMIN)
            throw new ForbiddenOperationException("SYSTEM_ADMIN role is required");
    }
}
