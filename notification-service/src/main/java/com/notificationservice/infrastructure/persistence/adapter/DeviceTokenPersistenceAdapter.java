package com.notificationservice.infrastructure.persistence.adapter;

import com.notificationservice.domain.model.*;
import com.notificationservice.domain.repository.DeviceTokenRepository;
import com.notificationservice.infrastructure.persistence.entity.DeviceTokenEntity;
import com.notificationservice.infrastructure.persistence.repository.JpaDeviceTokenRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class DeviceTokenPersistenceAdapter implements DeviceTokenRepository {
    private final JpaDeviceTokenRepository repository;
    public DeviceTokenPersistenceAdapter(JpaDeviceTokenRepository repository) { this.repository = repository; }
    public DeviceToken save(DeviceToken token) { return toDomain(repository.save(toEntity(token))); }
    public Optional<DeviceToken> findByToken(String token) { return repository.findByToken(token).map(this::toDomain); }
    public List<DeviceToken> findActiveByUser(UUID userId) { return repository.findByUserIdAndActiveTrue(userId).stream().map(this::toDomain).toList(); }
    private DeviceTokenEntity toEntity(DeviceToken token) { var entity = new DeviceTokenEntity(); entity.id = token.id(); entity.userId = token.userId(); entity.token = token.token(); entity.platform = token.platform(); entity.deviceIdentifier = token.deviceIdentifier(); entity.active = token.active(); entity.createdAt = token.createdAt(); entity.updatedAt = token.updatedAt(); return entity; }
    private DeviceToken toDomain(DeviceTokenEntity entity) { return new DeviceToken(entity.id, entity.userId, entity.token, entity.platform, entity.deviceIdentifier, entity.active, entity.createdAt, entity.updatedAt); }
}
