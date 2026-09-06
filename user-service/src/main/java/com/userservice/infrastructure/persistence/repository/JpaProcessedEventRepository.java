package com.userservice.infrastructure.persistence.repository;

import com.userservice.infrastructure.persistence.entity.ProcessedEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaProcessedEventRepository extends JpaRepository<ProcessedEventEntity, UUID> {
}
