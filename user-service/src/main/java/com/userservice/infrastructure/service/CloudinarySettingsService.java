package com.userservice.infrastructure.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.userservice.infrastructure.config.CloudinaryProperties;
import com.userservice.infrastructure.persistence.entity.CloudinarySettingsEntity;
import com.userservice.infrastructure.persistence.repository.JpaCloudinarySettingsRepository;
import com.userservice.infrastructure.security.CloudinarySecretProtector;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@Service
public class CloudinarySettingsService {
    private static final UUID ID=UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final JpaCloudinarySettingsRepository repository; private final CloudinaryProperties environment; private final CloudinarySecretProtector protector;
    public CloudinarySettingsService(JpaCloudinarySettingsRepository r,CloudinaryProperties e,CloudinarySecretProtector p){repository=r;environment=e;protector=p;}
    public Snapshot current(){var s=repository.findById(ID).orElse(null);return s==null?new Snapshot(environment.cloudName(),environment.apiKey(),environment.apiSecret(),"environment"):new Snapshot(s.getCloudName(),s.getApiKey(),protector.decrypt(s.getApiSecretEncrypted()),"database");}
    public Snapshot save(String cloudName,String apiKey,String apiSecret,UUID userId){if(blank(cloudName)||blank(apiKey)||blank(apiSecret))throw new IllegalArgumentException("Cloudinary cloud name, API key and API secret are required");repository.save(new CloudinarySettingsEntity(ID,cloudName.trim(),apiKey.trim(),protector.encrypt(apiSecret.trim()),Instant.now(),userId));return current();}
    public Cloudinary client(){var s=current();if(!s.configured())throw new IllegalArgumentException("Cloudinary is not configured");return new Cloudinary(ObjectUtils.asMap("cloud_name",s.cloudName(),"api_key",s.apiKey(),"api_secret",s.apiSecret()));}
    private static boolean blank(String v){return v==null||v.isBlank();}
    public record Snapshot(String cloudName,String apiKey,String apiSecret,String source){public boolean configured(){return !blank(cloudName)&&!blank(apiKey)&&!blank(apiSecret);}public boolean apiKeyConfigured(){return !blank(apiKey);}public boolean apiSecretConfigured(){return !blank(apiSecret);}}
}
