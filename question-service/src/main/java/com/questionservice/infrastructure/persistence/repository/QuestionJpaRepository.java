package com.questionservice.infrastructure.persistence.repository;
import com.questionservice.infrastructure.persistence.entity.QuestionEntity; import java.util.UUID; import org.springframework.data.jpa.repository.*; import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
public interface QuestionJpaRepository extends JpaRepository<QuestionEntity,UUID>, JpaSpecificationExecutor<QuestionEntity> { boolean existsByAiSourceId(String id); }
