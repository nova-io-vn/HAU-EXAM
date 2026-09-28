package com.questionservice.infrastructure.persistence.adapter;

import com.questionservice.application.port.out.QuestionAssignmentRepository;
import com.questionservice.domain.model.QuestionAssignment;
import com.questionservice.infrastructure.persistence.entity.QuestionAssignmentEntity;
import com.questionservice.infrastructure.persistence.repository.QuestionAssignmentJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class QuestionAssignmentPersistenceAdapter implements QuestionAssignmentRepository {
    private final QuestionAssignmentJpaRepository repository;
    public QuestionAssignmentPersistenceAdapter(QuestionAssignmentJpaRepository repository) { this.repository = repository; }
    public QuestionAssignment save(QuestionAssignment value) {
        var e = new QuestionAssignmentEntity(); e.id=value.id();e.facultyId=value.facultyId();e.subjectId=value.subjectId();
        e.chapterId=value.chapterId();e.topicId=value.topicId();e.knowledgeItemId=value.knowledgeItemId();e.lecturerId=value.lecturerId();
        e.assignedBy=value.assignedBy();e.requiredQuestionCount=value.requiredQuestionCount();e.requiredEasy=value.requiredEasy();
        e.requiredMedium=value.requiredMedium();e.requiredHard=value.requiredHard();e.deadline=value.deadline();e.note=value.note();
        e.createdAt=value.createdAt();e.updatedAt=value.updatedAt();
        repository.findById(value.id()).ifPresent(old -> e.version = old.version);
        return domain(repository.save(e));
    }
    public Optional<QuestionAssignment> findById(UUID id) { return repository.findById(id).map(this::domain); }
    public List<QuestionAssignment> findByFaculty(String facultyId) { return repository.findAllByFacultyIdOrderByDeadlineAsc(facultyId).stream().map(this::domain).toList(); }
    public List<QuestionAssignment> findByLecturer(UUID lecturerId) { return repository.findAllByLecturerIdOrderByDeadlineAsc(lecturerId).stream().map(this::domain).toList(); }
    private QuestionAssignment domain(QuestionAssignmentEntity e) { return new QuestionAssignment(e.id,e.facultyId,e.subjectId,e.chapterId,e.topicId,e.knowledgeItemId,e.lecturerId,e.assignedBy,e.requiredQuestionCount,e.requiredEasy,e.requiredMedium,e.requiredHard,e.deadline,e.note,e.createdAt,e.updatedAt); }
}
