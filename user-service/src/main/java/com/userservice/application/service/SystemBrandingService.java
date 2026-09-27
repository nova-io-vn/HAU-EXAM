package com.userservice.application.service;

import com.userservice.application.model.ImageUploadCommand;
import com.userservice.application.model.SystemBranding;
import com.userservice.application.port.out.SystemBrandingRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemBrandingService {
    private final SystemBrandingRepository repository;
    private final ImageStorageService images;

    public SystemBrandingService(SystemBrandingRepository repository, ImageStorageService images) {
        this.repository = repository;
        this.images = images;
    }

    @Transactional(readOnly = true)
    public SystemBranding current() {
        return repository.find().orElseGet(SystemBranding::defaults);
    }

    @Transactional
    public SystemBranding update(String systemName, String shortName, UUID actor) {
        var current = current();
        return repository.save(new SystemBranding(systemName.trim(), shortName.trim(), current.logoUrl(),
                current.faviconUrl(), actor, Instant.now()));
    }

    @Transactional
    public SystemBranding uploadLogo(ImageUploadCommand command, UUID actor) {
        var current = current();
        var stored = images.upload(command, "hau-exam/branding");
        String publicUrl = stored.secureUrl() != null ? stored.secureUrl() : stored.url();
        return repository.save(new SystemBranding(current.systemName(), current.shortName(), publicUrl,
                current.faviconUrl(), actor, Instant.now()));
    }

    @Transactional
    public SystemBranding uploadFavicon(ImageUploadCommand command, UUID actor) {
        var current = current();
        var stored = images.upload(command, "hau-exam/branding");
        String publicUrl = stored.secureUrl() != null ? stored.secureUrl() : stored.url();
        return repository.save(new SystemBranding(current.systemName(), current.shortName(), current.logoUrl(),
                publicUrl, actor, Instant.now()));
    }
}
