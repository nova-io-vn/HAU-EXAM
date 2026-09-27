package com.notificationservice.infrastructure.persistence.repository;
import com.notificationservice.infrastructure.persistence.entity.TelegramConnectionEntity; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface JpaTelegramConnectionRepository extends JpaRepository<TelegramConnectionEntity,UUID>{Optional<TelegramConnectionEntity> findByChatId(String chatId);}
