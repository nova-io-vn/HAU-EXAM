package com.aiservice.infrastructure.persistence.repository;
import com.aiservice.infrastructure.persistence.entity.TextbookStructureDraftEntity;import java.util.*;import org.springframework.data.jpa.repository.JpaRepository;
public interface TextbookStructureDraftRepository extends JpaRepository<TextbookStructureDraftEntity,UUID>{List<TextbookStructureDraftEntity>findAllByOwnerIdAndSubjectIdOrderByCreatedAtDesc(UUID ownerId,UUID subjectId);}
