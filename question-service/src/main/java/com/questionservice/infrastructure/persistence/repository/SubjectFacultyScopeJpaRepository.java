package com.questionservice.infrastructure.persistence.repository;

import com.questionservice.infrastructure.persistence.entity.SubjectFacultyScopeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubjectFacultyScopeJpaRepository extends JpaRepository<SubjectFacultyScopeEntity, UUID> {
    List<SubjectFacultyScopeEntity> findAllBySubjectId(UUID subjectId);
    List<SubjectFacultyScopeEntity> findAllBySubjectIdAndActiveTrue(UUID subjectId);
    List<SubjectFacultyScopeEntity> findAllByFacultyIdAndActiveTrue(String facultyId);
    Optional<SubjectFacultyScopeEntity> findBySubjectIdAndFacultyId(UUID subjectId, String facultyId);
}
