package com.hau.auth.infrastructure.persistence.adapter;

import com.hau.auth.domain.model.AuthAccount;
import com.hau.auth.domain.repository.AuthAccountRepository;
import com.hau.auth.infrastructure.persistence.mapper.AuthAccountPersistenceMapper;
import com.hau.auth.infrastructure.persistence.repository.JpaAuthAccountRepository;
import org.springframework.stereotype.Repository;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AuthAccountPersistenceAdapter implements AuthAccountRepository {

    private final JpaAuthAccountRepository repository;
    private final AuthAccountPersistenceMapper mapper;

    public AuthAccountPersistenceAdapter(
            JpaAuthAccountRepository repository,
            AuthAccountPersistenceMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public AuthAccount save(AuthAccount account) {
        return mapper.toDomain(repository.save(mapper.toEntity(account)));
    }

    @Override
    public Optional<AuthAccount> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<AuthAccount> findByLecturerCode(String lecturerCode) {
        return repository.findByLecturerCode(normalize(lecturerCode)).map(mapper::toDomain);
    }

    @Override
    public boolean existsByLecturerCode(String lecturerCode) {
        return repository.existsByLecturerCode(normalize(lecturerCode));
    }

    private String normalize(String lecturerCode) {
        return lecturerCode == null ? null : lecturerCode.trim().toUpperCase(Locale.ROOT);
    }
}
