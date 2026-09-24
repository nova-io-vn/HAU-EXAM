package com.aiservice.infrastructure.persistence.repository;
import com.aiservice.infrastructure.persistence.entity.AiSettingsEntity; import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface AiSettingsRepository extends JpaRepository<AiSettingsEntity,UUID> {}
