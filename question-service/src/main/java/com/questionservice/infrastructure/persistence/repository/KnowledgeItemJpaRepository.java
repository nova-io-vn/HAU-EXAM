package com.questionservice.infrastructure.persistence.repository;

import com.questionservice.infrastructure.persistence.entity.KnowledgeItemEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeItemJpaRepository extends JpaRepository<KnowledgeItemEntity, UUID> {
    List<KnowledgeItemEntity> findAllByTopicIdOrderByOrdinalAscCodeAsc(UUID topicId);
}
