package com.userservice.application.model;

import java.time.Instant;
import java.util.UUID;

public record SystemBranding(String systemName, String shortName, String logoUrl, String faviconUrl,
                             UUID updatedBy, Instant updatedAt) {
    public static SystemBranding defaults() {
        return new SystemBranding("HAU QM", "HAU QM", null, null, null, null);
    }
}
