package com.aiservice.infrastructure.persistence.repository;
import com.aiservice.infrastructure.persistence.entity.ChatConversationEntity; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface ChatConversationJpaRepository extends JpaRepository<ChatConversationEntity,UUID> { List<ChatConversationEntity> findByUserIdOrderByUpdatedAtDesc(UUID userId); }
