package com.notificationservice.infrastructure.persistence.repository;

import com.notificationservice.infrastructure.persistence.entity.EmailSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface JpaEmailSettingsRepository extends JpaRepository<EmailSettingsEntity, UUID> {
}
