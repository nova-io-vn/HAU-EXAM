package com.authservice.infrastructure.persistence.repository;

import com.authservice.infrastructure.persistence.entity.ProcessedAuthEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaProcessedAuthEventRepository extends JpaRepository<ProcessedAuthEventEntity, UUID> { }
