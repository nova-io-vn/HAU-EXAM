package com.aiservice.infrastructure.persistence.repository;
import com.aiservice.infrastructure.persistence.entity.ChatAttachmentEntity; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface ChatAttachmentJpaRepository extends JpaRepository<ChatAttachmentEntity,UUID> { List<ChatAttachmentEntity> findByConversationId(UUID id); void deleteByConversationIdAndDocumentId(UUID conversationId, UUID documentId); }
