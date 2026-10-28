package com.questionservice.infrastructure.persistence.adapter;

import com.questionservice.infrastructure.persistence.entity.QuestionAssignmentEntity;
import com.questionservice.infrastructure.persistence.repository.QuestionAssignmentJpaRepository;
import com.questionservice.infrastructure.persistence.repository.SubjectLecturerAssignmentJpaRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SubjectAssignmentPersistenceAdapterTest {
    private final SubjectLecturerAssignmentJpaRepository direct = mock(SubjectLecturerAssignmentJpaRepository.class);
    private final QuestionAssignmentJpaRepository questionAssignments = mock(QuestionAssignmentJpaRepository.class);
    private final SubjectAssignmentPersistenceAdapter adapter = new SubjectAssignmentPersistenceAdapter(direct, questionAssignments);
    private final UUID subjectId = UUID.randomUUID();
    private final UUID lecturerId = UUID.randomUUID();

    @Test
    void existingQuestionAssignmentGrantsSubjectReadAndCreationAccess() {
        var assignment = new QuestionAssignmentEntity();
        assignment.subjectId = subjectId;
        assignment.lecturerId = lecturerId;
        when(questionAssignments.existsBySubjectIdAndLecturerId(subjectId, lecturerId)).thenReturn(true);
        when(questionAssignments.findAllByLecturerIdOrderByDeadlineAsc(lecturerId)).thenReturn(List.of(assignment));
        when(questionAssignments.findAllBySubjectId(subjectId)).thenReturn(List.of(assignment));

        assertTrue(adapter.existsActive(subjectId, lecturerId));
        assertEquals(List.of(subjectId), adapter.findActiveSubjectIds(lecturerId));
        assertEquals(List.of(lecturerId), adapter.findActiveUserIds(subjectId));
    }
}
