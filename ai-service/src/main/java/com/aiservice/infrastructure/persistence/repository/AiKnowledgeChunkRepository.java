package com.aiservice.infrastructure.persistence.repository;
import com.aiservice.infrastructure.persistence.entity.AiKnowledgeChunkEntity; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface AiKnowledgeChunkRepository extends JpaRepository<AiKnowledgeChunkEntity,UUID>{List<AiKnowledgeChunkEntity> findByDocumentIdOrderByChunkIndex(UUID documentId);long countByDocumentId(UUID documentId);void deleteByDocumentId(UUID documentId);}
