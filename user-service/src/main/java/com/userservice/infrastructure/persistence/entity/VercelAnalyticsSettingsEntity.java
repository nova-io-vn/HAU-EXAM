package com.userservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vercel_analytics_settings")
public class VercelAnalyticsSettingsEntity {
    @Id private UUID id;
    @Column(name = "project_id", nullable = false, length = 255) private String projectId;
    @Column(name = "team_id", nullable = false, length = 255) private String teamId;
    @Column(name = "token_encrypted", nullable = false, columnDefinition = "TEXT") private String tokenEncrypted;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Column(name = "updated_by", nullable = false) private UUID updatedBy;
    protected VercelAnalyticsSettingsEntity() { }
    public VercelAnalyticsSettingsEntity(UUID id,String projectId,String teamId,String tokenEncrypted,Instant updatedAt,UUID updatedBy){this.id=id;this.projectId=projectId;this.teamId=teamId;this.tokenEncrypted=tokenEncrypted;this.updatedAt=updatedAt;this.updatedBy=updatedBy;}
    public String getProjectId(){return projectId;} public String getTeamId(){return teamId;} public String getTokenEncrypted(){return tokenEncrypted;}
}
