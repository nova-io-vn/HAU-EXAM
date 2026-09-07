package com.userservice.infrastructure.persistence.adapter;

import com.userservice.domain.model.UserProfile;
import com.userservice.domain.repository.*;
import com.userservice.infrastructure.persistence.mapper.UserProfilePersistenceMapper;
import com.userservice.infrastructure.persistence.entity.UserProfileEntity;
import com.userservice.infrastructure.persistence.repository.JpaUserProfileRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

import com.userservice.domain.model.Role;
import com.userservice.domain.model.UserStatus;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

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
        String keyword=blankToNull(q.keyword()); String facultyId=blankToNull(q.facultyId());
        Specification<UserProfileEntity> spec=profileSpecification(keyword,q.role(),q.status(),facultyId);
        var p = repository.findAll(spec, PageRequest.of(Math.max(0,q.page()),Math.min(100,Math.max(1,q.size())))).map(mapper::toDomain);
        return new PageResult<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
    }

    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    public List<UserProfile> findActiveAudience(Role role, String facultyId) {
        String faculty = facultyId == null || facultyId.isBlank() ? null : facultyId.trim();
        return repository.findAll(profileSpecification(null,role,UserStatus.ACTIVE,faculty)).stream().map(mapper::toDomain).toList();
    }

    private Specification<UserProfileEntity> profileSpecification(String keyword, Role role, UserStatus status, String facultyId){
        return (root,query,cb)->{var predicates=new java.util.ArrayList<Predicate>();
            if(keyword!=null){String pattern="%"+keyword.toLowerCase(Locale.ROOT)+"%";predicates.add(cb.or(cb.like(cb.lower(root.<String>get("lecturerCode")),pattern),cb.like(cb.lower(root.<String>get("fullName")),pattern),cb.like(cb.lower(root.<String>get("email")),pattern)));}
            if(role!=null)predicates.add(cb.equal(root.get("role"),role));
            if(status!=null)predicates.add(cb.equal(root.get("status"),status));
            if(facultyId!=null)predicates.add(cb.equal(root.get("facultyId"),facultyId));
            return cb.and(predicates.toArray(Predicate[]::new));};
    }

    private String normalize(String v) {
        return v == null ? null : v.trim().toUpperCase(Locale.ROOT);
    }
}
