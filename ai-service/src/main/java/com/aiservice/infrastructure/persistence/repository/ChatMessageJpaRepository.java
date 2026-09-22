package com.aiservice.infrastructure.persistence.repository;
import com.aiservice.infrastructure.persistence.entity.ChatMessageEntity; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface ChatMessageJpaRepository extends JpaRepository<ChatMessageEntity,UUID> { List<ChatMessageEntity> findByConversationIdOrderByCreatedAtAsc(UUID id); }
