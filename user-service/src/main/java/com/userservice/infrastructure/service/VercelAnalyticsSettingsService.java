package com.userservice.infrastructure.service;

import com.userservice.infrastructure.config.VercelAnalyticsProperties;
import com.userservice.infrastructure.persistence.entity.VercelAnalyticsSettingsEntity;
import com.userservice.infrastructure.persistence.repository.JpaVercelAnalyticsSettingsRepository;
import com.userservice.infrastructure.security.CloudinarySecretProtector;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@Service
public class VercelAnalyticsSettingsService {
    private static final UUID ID=UUID.fromString("00000000-0000-0000-0000-000000000002");
    private final JpaVercelAnalyticsSettingsRepository repository; private final VercelAnalyticsProperties environment; private final CloudinarySecretProtector protector;
    public VercelAnalyticsSettingsService(JpaVercelAnalyticsSettingsRepository r,VercelAnalyticsProperties e,CloudinarySecretProtector p){repository=r;environment=e;protector=p;}
    public Snapshot current(){var s=repository.findById(ID).orElse(null);return s==null?new Snapshot(environment.projectId(),environment.teamId(),environment.token(),"environment"):new Snapshot(s.getProjectId(),s.getTeamId(),protector.decrypt(s.getTokenEncrypted()),"database");}
    public Snapshot save(String projectId,String teamId,String token,UUID userId){if(blank(projectId)||blank(teamId))throw new IllegalArgumentException("Vercel project ID and team ID are required");var old=repository.findById(ID).orElse(null);String effective=blank(token)?old==null?"":protector.decrypt(old.getTokenEncrypted()):token.trim();if(blank(effective))throw new IllegalArgumentException("Vercel token is required");repository.save(new VercelAnalyticsSettingsEntity(ID,projectId.trim(),teamId.trim(),protector.encrypt(effective),Instant.now(),userId));return current();}
    private static boolean blank(String v){return v==null||v.isBlank();}
    public record Snapshot(String projectId,String teamId,String token,String source){public boolean configured(){return !blank(projectId)&&!blank(teamId)&&!blank(token);}public boolean tokenConfigured(){return !blank(token);}}
}
