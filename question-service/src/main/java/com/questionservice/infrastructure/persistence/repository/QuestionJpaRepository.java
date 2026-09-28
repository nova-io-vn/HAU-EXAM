package com.questionservice.infrastructure.persistence.repository;

import com.questionservice.infrastructure.persistence.entity.QuestionEntity;

import java.util.UUID;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface QuestionJpaRepository extends JpaRepository<QuestionEntity, UUID>, JpaSpecificationExecutor<QuestionEntity> {
    boolean existsByAiSourceId(String id);

    long countByFacultyId(String facultyId);

    long countByFacultyIdAndStatus(String facultyId, com.questionservice.domain.model.QuestionStatus status);

    long countByCreatedBy(UUID createdBy);

    long countByCreatedByAndStatus(UUID createdBy, com.questionservice.domain.model.QuestionStatus status);

    long countByStatus(com.questionservice.domain.model.QuestionStatus status);

    long countByAssignmentId(UUID assignmentId);
    long countByAssignmentIdAndStatusIn(UUID assignmentId, java.util.Collection<com.questionservice.domain.model.QuestionStatus> statuses);
    long countByAssignmentIdAndStatus(UUID assignmentId, com.questionservice.domain.model.QuestionStatus status);
    long countByAssignmentIdAndStatusAndDifficulty(UUID assignmentId, com.questionservice.domain.model.QuestionStatus status, com.questionservice.domain.model.Difficulty difficulty);

    @Query("select q.knowledgeItemId, q.difficulty, count(q) from QuestionEntity q where q.subjectId = :subjectId and q.status = com.questionservice.domain.model.QuestionStatus.APPROVED and q.knowledgeItemId is not null group by q.knowledgeItemId, q.difficulty")
    java.util.List<Object[]> approvedCoverage(@Param("subjectId") UUID subjectId);
}
