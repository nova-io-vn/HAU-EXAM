package com.questionservice.infrastructure.persistence.adapter;

import com.questionservice.application.port.out.SubjectAssignmentRepository;
import com.questionservice.infrastructure.persistence.entity.SubjectLecturerAssignmentEntity;
import com.questionservice.infrastructure.persistence.repository.SubjectLecturerAssignmentJpaRepository;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.UUID;
import java.util.List;

@Component
public class SubjectAssignmentPersistenceAdapter implements SubjectAssignmentRepository {
    private final SubjectLecturerAssignmentJpaRepository repository;
    public SubjectAssignmentPersistenceAdapter(SubjectLecturerAssignmentJpaRepository repository) { this.repository = repository; }
    public boolean existsActive(UUID subjectId, UUID userId) { return repository.existsBySubjectIdAndUserIdAndActiveTrue(subjectId, userId); }
    public List<UUID> findActiveSubjectIds(UUID userId) { return repository.findAllByUserIdAndActiveTrue(userId).stream().map(e -> e.subjectId).toList(); }
    public List<UUID> findActiveUserIds(UUID subjectId) { return repository.findAllBySubjectIdAndActiveTrue(subjectId).stream().map(e -> e.userId).toList(); }
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
