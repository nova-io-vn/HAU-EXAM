package com.userservice.infrastructure.persistence.adapter;

import com.userservice.application.model.SystemBranding;
import com.userservice.application.port.out.SystemBrandingRepository;
import com.userservice.infrastructure.persistence.entity.SystemBrandingEntity;
import com.userservice.infrastructure.persistence.repository.JpaSystemBrandingRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class SystemBrandingPersistenceAdapter implements SystemBrandingRepository {
    private static final UUID BRANDING_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final JpaSystemBrandingRepository repository;

    public SystemBrandingPersistenceAdapter(JpaSystemBrandingRepository repository) { this.repository = repository; }

    @Override public Optional<SystemBranding> find() { return repository.findById(BRANDING_ID).map(this::toDomain); }

    @Override public SystemBranding save(SystemBranding value) {
        var entity = new SystemBrandingEntity();
        entity.id = BRANDING_ID;
        entity.systemName = value.systemName();
        entity.shortName = value.shortName();
        entity.logoUrl = value.logoUrl();
        entity.faviconUrl = value.faviconUrl();
        entity.updatedBy = value.updatedBy();
        entity.updatedAt = value.updatedAt();
        return toDomain(repository.save(entity));
    }

    private SystemBranding toDomain(SystemBrandingEntity value) {
        return new SystemBranding(value.systemName, value.shortName, value.logoUrl, value.faviconUrl,
                value.updatedBy, value.updatedAt);
    }
}
