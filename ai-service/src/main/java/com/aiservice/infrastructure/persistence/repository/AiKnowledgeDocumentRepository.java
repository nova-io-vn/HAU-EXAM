package com.aiservice.infrastructure.persistence.repository;
import com.aiservice.infrastructure.persistence.entity.AiKnowledgeDocumentEntity; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface AiKnowledgeDocumentRepository extends JpaRepository<AiKnowledgeDocumentEntity,UUID>{List<AiKnowledgeDocumentEntity> findByEnabledTrueAndStatusOrderByUpdatedAtDesc(String status);}
