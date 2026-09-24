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
    private final UserProfileRepository users;

    public FacultyService(FacultyRepository repo, Clock clock) { this(repo, clock, null); }
    @org.springframework.beans.factory.annotation.Autowired
    public FacultyService(FacultyRepository repo, Clock clock, UserProfileRepository users) {
        this.repo = repo; this.clock = clock; this.users = users;
    }

    @Transactional(readOnly = true)
    public PageResult<Faculty> search(ActorContext a, FacultyQuery q) {
        admin(a);
        return repo.search(q);
    }

    @Transactional(readOnly = true)
    public PageResult<Faculty> publicActive(FacultyQuery q) {
        return repo.search(new FacultyQuery(q.keyword(), true, q.page(), q.size()));
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

    public com.userservice.presentation.response.FacultyResponse response(Faculty f) {
        if (users == null) return com.userservice.presentation.response.FacultyResponse.from(f);
        var all = users.findAll(new PageQuery(0, 1, null, f.code(), null, UserStatus.ACTIVE, null));
        var admins = users.findActiveAudience(Role.SUBJECT_ADMIN, f.code()).stream().map(u ->
                new com.userservice.presentation.response.FacultyResponse.SubjectAdmin(u.getId(), u.getFullName(), u.getLecturerCode(), u.getAvatar(), u.getFacultyId())).toList();
        return new com.userservice.presentation.response.FacultyResponse(f.id(), f.code(), f.name(), f.description(), f.active(), f.createdAt(), f.updatedAt(), all.totalElements(), admins);
    }

    private void admin(ActorContext a) {
        if (a == null || a.role() != Role.SYSTEM_ADMIN)
            throw new ForbiddenOperationException("SYSTEM_ADMIN role is required");
    }
}
