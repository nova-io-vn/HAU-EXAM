package com.userservice.infrastructure.persistence.repository;

import com.userservice.infrastructure.persistence.entity.SystemBrandingEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaSystemBrandingRepository extends JpaRepository<SystemBrandingEntity, UUID> { }
