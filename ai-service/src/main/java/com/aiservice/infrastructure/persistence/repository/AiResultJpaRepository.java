package com.aiservice.infrastructure.persistence.repository;

import com.aiservice.infrastructure.persistence.entity.AiResultEntity;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AiResultJpaRepository extends JpaRepository<AiResultEntity, UUID> {
    Optional<AiResultEntity> findByJobId(UUID jobId);
    List<AiResultEntity> findByJobIdIn(Set<UUID> jobIds);
}
