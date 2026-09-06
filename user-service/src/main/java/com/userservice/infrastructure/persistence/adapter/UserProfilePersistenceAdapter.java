package com.userservice.infrastructure.persistence.adapter;

import com.userservice.domain.model.UserProfile;
import com.userservice.domain.repository.*;
import com.userservice.infrastructure.persistence.mapper.UserProfilePersistenceMapper;
import com.userservice.infrastructure.persistence.repository.JpaUserProfileRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

import com.userservice.domain.model.Role;
import com.userservice.domain.model.UserStatus;

@Repository
public class UserProfilePersistenceAdapter implements UserProfileRepository {
    private final JpaUserProfileRepository repository;
    private final UserProfilePersistenceMapper mapper;

    public UserProfilePersistenceAdapter(JpaUserProfileRepository repository, UserProfilePersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public UserProfile save(UserProfile profile) {
        return mapper.toDomain(repository.save(mapper.toEntity(profile)));
    }

    public Optional<UserProfile> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    public Optional<UserProfile> findByLecturerCode(String code) {
        return repository.findByLecturerCode(normalize(code)).map(mapper::toDomain);
    }

    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    public boolean existsByLecturerCode(String code) {
        return repository.existsByLecturerCode(normalize(code));
    }

    public boolean existsByEmail(String email) {
        return email != null && repository.existsByEmailIgnoreCase(email.trim());
    }

    public PageResult<UserProfile> findAll(PageQuery q) {
        var p = repository.findAll(PageRequest.of(q.page(), q.size())).map(mapper::toDomain);
        return new PageResult<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
    }

    public List<UserProfile> findActiveAudience(Role role, String facultyId) {
        String faculty = facultyId == null || facultyId.isBlank() ? null : facultyId.trim();
        return repository.findAudience(UserStatus.ACTIVE, role, faculty).stream().map(mapper::toDomain).toList();
    }

    private String normalize(String v) {
        return v == null ? null : v.trim().toUpperCase(Locale.ROOT);
    }
}
