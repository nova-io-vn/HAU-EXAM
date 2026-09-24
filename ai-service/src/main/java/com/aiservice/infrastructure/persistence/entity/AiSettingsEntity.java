package com.aiservice.infrastructure.persistence.entity;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="ai_settings") public class AiSettingsEntity { @Id public UUID id; @Column(nullable=false,length=20) public String provider; @Column(nullable=false,length=160) public String model; @Column(name="api_key_encrypted",columnDefinition="TEXT") public String apiKeyEncrypted; @Column(name="updated_at",nullable=false) public Instant updatedAt; @Column(name="updated_by",nullable=false) public UUID updatedBy; }
