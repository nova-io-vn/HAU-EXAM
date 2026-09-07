package com.userservice.domain.repository;

import com.userservice.domain.model.Faculty;
import java.util.Optional;
import java.util.UUID;

public interface FacultyRepository {
    PageResult<Faculty> search(FacultyQuery query);
    Optional<Faculty> findById(UUID id);
    Optional<Faculty> findByCode(String code);
    Faculty save(Faculty faculty);
}
