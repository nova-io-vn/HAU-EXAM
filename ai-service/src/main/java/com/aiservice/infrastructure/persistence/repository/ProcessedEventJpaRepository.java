package com.aiservice.infrastructure.persistence.repository;

import com.aiservice.infrastructure.persistence.entity.ProcessedEventEntity;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventJpaRepository extends JpaRepository<ProcessedEventEntity, UUID> {
}
