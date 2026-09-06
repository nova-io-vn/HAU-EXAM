package com.notificationservice.infrastructure.persistence.repository;

import com.notificationservice.infrastructure.persistence.entity.DeviceTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface JpaDeviceTokenRepository extends JpaRepository<DeviceTokenEntity, UUID> {
    Optional<DeviceTokenEntity> findByToken(String token);
    List<DeviceTokenEntity> findByUserIdAndActiveTrue(UUID userId);
}
