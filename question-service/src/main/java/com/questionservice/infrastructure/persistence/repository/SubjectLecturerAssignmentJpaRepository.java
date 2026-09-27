package com.questionservice.infrastructure.persistence.repository;

import com.questionservice.infrastructure.persistence.entity.SubjectLecturerAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface SubjectLecturerAssignmentJpaRepository extends JpaRepository<SubjectLecturerAssignmentEntity, UUID> {
    Optional<SubjectLecturerAssignmentEntity> findBySubjectIdAndUserId(UUID subjectId, UUID userId);
    boolean existsBySubjectIdAndUserIdAndActiveTrue(UUID subjectId, UUID userId);
    List<SubjectLecturerAssignmentEntity> findAllByUserIdAndActiveTrue(UUID userId);
    List<SubjectLecturerAssignmentEntity> findAllBySubjectIdAndActiveTrue(UUID subjectId);
}
