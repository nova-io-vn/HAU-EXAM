package com.userservice.infrastructure.persistence.repository;

import com.userservice.infrastructure.persistence.entity.CloudinarySettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface JpaCloudinarySettingsRepository extends JpaRepository<CloudinarySettingsEntity, UUID> { }
