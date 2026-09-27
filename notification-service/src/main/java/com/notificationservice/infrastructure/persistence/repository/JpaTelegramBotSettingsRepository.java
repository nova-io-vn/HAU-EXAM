package com.notificationservice.infrastructure.persistence.repository;
import com.notificationservice.infrastructure.persistence.entity.TelegramBotSettingsEntity; import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface JpaTelegramBotSettingsRepository extends JpaRepository<TelegramBotSettingsEntity,UUID>{}
