package com.notificationservice.infrastructure.persistence.repository;
import com.notificationservice.infrastructure.persistence.entity.TelegramPreferenceEntity; import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface JpaTelegramPreferenceRepository extends JpaRepository<TelegramPreferenceEntity,UUID>{}
