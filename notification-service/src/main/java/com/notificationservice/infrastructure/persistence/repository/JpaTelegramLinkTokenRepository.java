package com.notificationservice.infrastructure.persistence.repository;
import com.notificationservice.infrastructure.persistence.entity.TelegramLinkTokenEntity; import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional;
public interface JpaTelegramLinkTokenRepository extends JpaRepository<TelegramLinkTokenEntity,String>{Optional<TelegramLinkTokenEntity> findByTokenHashAndConsumedAtIsNull(String hash);}
