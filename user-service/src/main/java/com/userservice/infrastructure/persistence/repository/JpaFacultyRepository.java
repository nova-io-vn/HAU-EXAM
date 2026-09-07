package com.userservice.infrastructure.persistence.repository;

import com.userservice.infrastructure.persistence.entity.FacultyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface JpaFacultyRepository extends JpaRepository<FacultyEntity, UUID>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<FacultyEntity> {
    Optional<FacultyEntity> findByCode(String code);
}
