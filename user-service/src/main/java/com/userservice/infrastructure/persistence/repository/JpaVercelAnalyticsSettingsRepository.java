package com.userservice.infrastructure.persistence.repository;

import com.userservice.infrastructure.persistence.entity.VercelAnalyticsSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface JpaVercelAnalyticsSettingsRepository extends JpaRepository<VercelAnalyticsSettingsEntity, UUID> { }
