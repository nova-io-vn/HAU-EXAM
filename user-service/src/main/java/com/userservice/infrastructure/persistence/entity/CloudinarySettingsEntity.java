package com.userservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cloudinary_settings")
public class CloudinarySettingsEntity {
    @Id private UUID id;
    @Column(name = "cloud_name", nullable = false, length = 255) private String cloudName;
    @Column(name = "api_key", nullable = false, length = 255) private String apiKey;
    @Column(name = "api_secret_encrypted", nullable = false, columnDefinition = "TEXT") private String apiSecretEncrypted;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Column(name = "updated_by", nullable = false) private UUID updatedBy;
    protected CloudinarySettingsEntity() { }
    public CloudinarySettingsEntity(UUID id, String cloudName, String apiKey, String apiSecretEncrypted, Instant updatedAt, UUID updatedBy) { this.id=id; this.cloudName=cloudName; this.apiKey=apiKey; this.apiSecretEncrypted=apiSecretEncrypted; this.updatedAt=updatedAt; this.updatedBy=updatedBy; }
    public UUID getId(){return id;} public String getCloudName(){return cloudName;} public String getApiKey(){return apiKey;} public String getApiSecretEncrypted(){return apiSecretEncrypted;} public Instant getUpdatedAt(){return updatedAt;} public UUID getUpdatedBy(){return updatedBy;}
}
