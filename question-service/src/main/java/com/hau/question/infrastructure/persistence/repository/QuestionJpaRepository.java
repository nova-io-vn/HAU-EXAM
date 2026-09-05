package com.hau.question.infrastructure.persistence.repository;
import com.hau.question.infrastructure.persistence.entity.QuestionEntity; import java.util.UUID; import org.springframework.data.jpa.repository.*; import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
public interface QuestionJpaRepository extends JpaRepository<QuestionEntity,UUID>, JpaSpecificationExecutor<QuestionEntity> { boolean existsByAiSourceId(String id); }
