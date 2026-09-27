package com.userservice.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "system_branding")
public class SystemBrandingEntity {
    @Id public UUID id;
    @Column(name = "system_name", nullable = false, length = 120) public String systemName;
    @Column(name = "short_name", nullable = false, length = 40) public String shortName;
    @Column(name = "logo_url", length = 1000) public String logoUrl;
    @Column(name = "favicon_url", length = 1000) public String faviconUrl;
    @Column(name = "updated_by") public UUID updatedBy;
    @Column(name = "updated_at", nullable = false) public Instant updatedAt;
}
