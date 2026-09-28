package com.questionservice.application.port.out;

import com.questionservice.domain.model.QuestionAssignment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionAssignmentRepository {
    QuestionAssignment save(QuestionAssignment assignment);
    Optional<QuestionAssignment> findById(UUID id);
    List<QuestionAssignment> findByFaculty(String facultyId);
    List<QuestionAssignment> findByLecturer(UUID lecturerId);
}
