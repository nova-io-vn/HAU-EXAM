package com.questionservice.infrastructure.persistence.repository;

import com.questionservice.infrastructure.persistence.entity.QuestionAssignmentEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionAssignmentJpaRepository extends JpaRepository<QuestionAssignmentEntity, UUID> {
    List<QuestionAssignmentEntity> findAllByFacultyIdOrderByDeadlineAsc(String facultyId);
    List<QuestionAssignmentEntity> findAllByLecturerIdOrderByDeadlineAsc(UUID lecturerId);
}
