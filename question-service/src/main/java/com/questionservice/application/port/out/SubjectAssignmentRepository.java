package com.questionservice.application.port.out;

import java.time.Instant;
import java.util.UUID;
import java.util.List;

public interface SubjectAssignmentRepository {
    boolean existsActive(UUID subjectId, UUID userId);
    List<UUID> findActiveSubjectIds(UUID userId);
    List<UUID> findActiveUserIds(UUID subjectId);
    void assign(UUID subjectId, UUID userId, UUID assignedBy, Instant assignedAt);
    void remove(UUID subjectId, UUID userId);
}
