package com.questionservice.infrastructure.persistence.adapter;

import com.questionservice.application.port.out.SubjectAssignmentRepository;
import com.questionservice.infrastructure.persistence.entity.SubjectLecturerAssignmentEntity;
import com.questionservice.infrastructure.persistence.repository.SubjectLecturerAssignmentJpaRepository;
import com.questionservice.infrastructure.persistence.repository.QuestionAssignmentJpaRepository;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.UUID;
import java.util.List;
import java.util.stream.Stream;

@Component
public class SubjectAssignmentPersistenceAdapter implements SubjectAssignmentRepository {
    private final SubjectLecturerAssignmentJpaRepository repository;
    private final QuestionAssignmentJpaRepository questionAssignments;
    public SubjectAssignmentPersistenceAdapter(SubjectLecturerAssignmentJpaRepository repository,
                                               QuestionAssignmentJpaRepository questionAssignments) {
        this.repository = repository;
        this.questionAssignments = questionAssignments;
    }
    public boolean existsActive(UUID subjectId, UUID userId) {
        return repository.existsBySubjectIdAndUserIdAndActiveTrue(subjectId, userId)
                || questionAssignments.existsBySubjectIdAndLecturerId(subjectId, userId);
    }
    public List<UUID> findActiveSubjectIds(UUID userId) {
        return Stream.concat(repository.findAllByUserIdAndActiveTrue(userId).stream().map(e -> e.subjectId),
                questionAssignments.findAllByLecturerIdOrderByDeadlineAsc(userId).stream().map(e -> e.subjectId))
                .distinct().toList();
    }
    public List<UUID> findActiveUserIds(UUID subjectId) {
        return Stream.concat(repository.findAllBySubjectIdAndActiveTrue(subjectId).stream().map(e -> e.userId),
                questionAssignments.findAllBySubjectId(subjectId).stream().map(e -> e.lecturerId))
                .distinct().toList();
    }
    public void assign(UUID subjectId, UUID userId, UUID assignedBy, Instant assignedAt) {
        var e = repository.findBySubjectIdAndUserId(subjectId, userId).orElseGet(SubjectLecturerAssignmentEntity::new);
        if (e.id == null) e.id = UUID.randomUUID();
        e.subjectId = subjectId; e.userId = userId; e.assignedBy = assignedBy; e.assignedAt = assignedAt; e.active = true;
        repository.save(e);
    }
    public void remove(UUID subjectId, UUID userId) {
        repository.findBySubjectIdAndUserId(subjectId, userId).ifPresent(e -> { e.active = false; repository.save(e); });
    }
}
