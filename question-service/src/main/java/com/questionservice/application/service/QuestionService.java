package com.questionservice.application.service;

import com.questionservice.application.model.*;
import com.questionservice.application.port.out.*;
import com.questionservice.domain.exception.*;
import com.questionservice.domain.model.*;

import java.time.*;
import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class QuestionService {
    private final QuestionRepository repository;
    private final QuestionEventPublisher publisher;
    private final Clock clock;
    private final CatalogRepository catalog;
    private final ImageStoragePort imageStorage;
    private final SubjectAssignmentRepository assignments;
    private final QuestionAssignmentRepository questionAssignments;

    public QuestionService(QuestionRepository repository, QuestionEventPublisher publisher, Clock clock) {
        this(repository, publisher, clock, null, null, null, null);
    }
    public QuestionService(QuestionRepository repository, QuestionEventPublisher publisher, Clock clock, CatalogRepository catalog) {
        this(repository, publisher, clock, catalog, null, null, null);
    }
    public QuestionService(QuestionRepository repository, QuestionEventPublisher publisher, Clock clock, CatalogRepository catalog, ImageStoragePort imageStorage) {
        this(repository, publisher, clock, catalog, imageStorage, null, null);
    }
    public QuestionService(QuestionRepository repository, QuestionEventPublisher publisher, Clock clock, CatalogRepository catalog, ImageStoragePort imageStorage, SubjectAssignmentRepository assignments) {
        this(repository, publisher, clock, catalog, imageStorage, assignments, null);
    }
    @org.springframework.beans.factory.annotation.Autowired
    public QuestionService(QuestionRepository repository, QuestionEventPublisher publisher, Clock clock, CatalogRepository catalog, ImageStoragePort imageStorage, SubjectAssignmentRepository assignments, QuestionAssignmentRepository questionAssignments) {
        this.repository = repository; this.publisher = publisher; this.clock = clock; this.catalog = catalog; this.imageStorage = imageStorage; this.assignments = assignments; this.questionAssignments = questionAssignments;
    }

    public Question create(Actor actor, QuestionInput in) {
        requireRole(actor, Role.USER);
        require(actor.userId() != null, "Authenticated user is required");
        if (actor.facultyId() == null || actor.facultyId().isBlank())
            throw new ForbiddenException("Question creator must have a faculty assignment");
        validateTaxonomy(in.subjectId(), in.chapterId(), in.topicId(), in.knowledgeItemId(), actor.facultyId());
        requireAssignment(in.subjectId(), actor);
        validateQuestionAssignment(in.assignmentId(), in.subjectId(), in.chapterId(), in.topicId(), in.knowledgeItemId(), actor);
        var q = Question.create(UUID.randomUUID(), actor.facultyId(), in.subjectId(), in.chapterId(), in.topicId(), in.knowledgeItemId(), in.assignmentId(), in.content(), in.imageUrl(), in.storageKey(), in.type(), in.difficulty(), QuestionSource.MANUAL, null, actor.userId(), withIds(in.options()), Instant.now(clock));
        return repository.save(q);
    }

    public Question update(UUID id, Actor actor, QuestionInput in) {
        var q = get(id);
        owner(q, actor);
        String oldImage = q.storageKey();
        var oldOptionImages = q.options().stream().map(QuestionOption::storageKey).filter(Objects::nonNull).toList();
        validateTaxonomy(in.subjectId(), in.chapterId(), in.topicId(), in.knowledgeItemId(), actor.facultyId());
        requireAssignment(in.subjectId(), actor);
        q.edit(in.content(), in.imageUrl(), in.storageKey(), in.type(), in.difficulty(), withIds(in.options()), Instant.now(clock));
        var saved = repository.save(q);
        if (oldImage != null && !Objects.equals(oldImage, saved.storageKey())) deleteImage(oldImage);
        saved.options().forEach(option -> oldOptionImages.stream().filter(old -> !Objects.equals(old, option.storageKey())).forEach(this::deleteImage));
        return saved;
    }

    public void delete(UUID id, Actor actor) {
        var q = get(id);
        owner(q, actor);
        if (q.status() != QuestionStatus.DRAFT) throw new InvalidTransitionException("Only DRAFT can be deleted");
        repository.delete(id);
        deleteImage(q.storageKey());
        q.options().forEach(option -> deleteImage(option.storageKey()));
    }

    public Question submit(UUID id, Actor actor, UUID correlationId) {
        var q = get(id);
        owner(q, actor);
        q.submit(Instant.now(clock));
        q = repository.save(q);
        publisher.publish("question.submitted", "QUESTION_SUBMITTED", q, correlationId);
        return q;
    }

    public Question approve(UUID id, Actor actor, String comment, UUID correlationId) {
        var q = reviewable(id, actor);
        q.approve(actor.userId(), comment, Instant.now(clock));
        q = repository.save(q);
        publisher.publish("question.approved", "QUESTION_APPROVED", q, correlationId);
        return q;
    }

    public Question reject(UUID id, Actor actor, String reason, UUID correlationId) {
        var q = reviewable(id, actor);
        q.reject(actor.userId(), reason, Instant.now(clock));
        q = repository.save(q);
        publisher.publish("question.rejected", "QUESTION_REJECTED", q, correlationId);
        return q;
    }

    public Question requestRevision(UUID id, Actor actor, String reason, UUID correlationId) {
        var q = reviewable(id, actor);
        q.requestRevision(actor.userId(), reason, Instant.now(clock));
        q = repository.save(q);
        publisher.publish("question.revision.requested", "QUESTION_REVISION_REQUESTED", q, correlationId);
        return q;
    }

    public BulkResult bulk(String action, Actor actor, List<UUID> ids, String reason, UUID correlationId) {
        List<Question> changed = new ArrayList<>();
        List<Map<String, String>> failed = new ArrayList<>();
        for (UUID id : ids == null ? List.<UUID>of() : ids) {
            try {
                Question q = get(id);
                if ("submit".equals(action)) {
                    owner(q, actor); q.submit(Instant.now(clock)); q = repository.save(q);
                } else {
                    facultyReviewer(q, actor);
                    if ("approve".equals(action)) q.approve(actor.userId(), reason, Instant.now(clock));
                    else if ("reject".equals(action)) q.reject(actor.userId(), reason, Instant.now(clock));
                    else if ("request-revision".equals(action)) q.requestRevision(actor.userId(), reason, Instant.now(clock));
                    else throw new IllegalArgumentException("Unsupported bulk action");
                    q = repository.save(q);
                }
                changed.add(q);
            } catch (RuntimeException ex) {
                failed.add(Map.of("id", id.toString(), "error", ex.getMessage() == null ? "Operation failed" : ex.getMessage()));
            }
        }
        if (!changed.isEmpty()) {
            String key = switch (action) {
                case "submit" -> "question.bulk.submitted";
                case "approve" -> "question.bulk.approved";
                case "reject" -> "question.bulk.rejected";
                case "request-revision" -> "question.bulk.revision.requested";
                default -> throw new IllegalArgumentException("Unsupported bulk action");
            };
            String type = switch (action) {
                case "submit" -> "QUESTIONS_SUBMITTED_FOR_REVIEW";
                case "approve" -> "QUESTIONS_BULK_APPROVED";
                case "reject" -> "QUESTIONS_BULK_REJECTED";
                case "request-revision" -> "QUESTIONS_BULK_NEED_REVISION";
                default -> throw new IllegalArgumentException("Unsupported bulk action");
            };
            publisher.publishBulk(key, type, actor, changed, correlationId);
        }
        return new BulkResult(changed.size(), failed);
    }

    public record BulkResult(int success, List<Map<String, String>> errors) {}

    public Question archive(UUID id, Actor actor) {
        var q = get(id);
        if (actor.role() == Role.USER) owner(q, actor);
        else facultyReviewer(q, actor);
        q.archive(Instant.now(clock));
        return repository.save(q);
    }

    @Transactional(readOnly = true)
    public Question get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Question not found"));
    }

    @Transactional(readOnly = true)
    public Question getForActor(UUID id, Actor actor) {
        var q = get(id);
        if (actor.role() == Role.USER) owner(q, actor);
        else if (actor.role() == Role.SUBJECT_ADMIN && (!Objects.equals(actor.facultyId(), q.facultyId())))
            throw new ForbiddenException("Question is outside faculty scope");
        return q;
    }

    @Transactional(readOnly = true)
    public PageResult<Question> search(Actor actor, QuestionCriteria c) {
        var scoped = actor.role() == Role.USER ? new QuestionCriteria(c.facultyId(), c.subjectId(), c.chapterId(), c.topicId(), c.difficulty(), c.status(), c.source(), actor.userId(), c.keyword(), c.page(), c.size(), c.sort()) : actor.role() == Role.SUBJECT_ADMIN ? new QuestionCriteria(actor.facultyId(), c.subjectId(), c.chapterId(), c.topicId(), c.difficulty(), c.status(), c.source(), c.createdBy(), c.keyword(), c.page(), c.size(), c.sort()) : c;
        return repository.search(scoped);
    }

    @Transactional(readOnly = true)
    public QuestionStatistics statistics(Actor actor) {
        if (actor == null || actor.role() == Role.SYSTEM_ADMIN) return repository.statistics(null, null);
        if (actor.role() == Role.USER) {
            if (actor.userId() == null) throw new ForbiddenException("Authenticated user is required");
            return repository.statistics(null, actor.userId());
        }
        if (actor.facultyId() == null || actor.facultyId().isBlank()) throw new ForbiddenException("Faculty assignment is required");
        return repository.statistics(actor.facultyId(), null);
    }

    private Question reviewable(UUID id, Actor a) {
        var q = get(id);
        facultyReviewer(q, a);
        return q;
    }

    private void facultyReviewer(Question q, Actor a) {
        requireRole(a, Role.SUBJECT_ADMIN);
        if (a.facultyId() == null || !a.facultyId().equals(q.facultyId()))
            throw new ForbiddenException("Question is outside reviewer faculty scope");
    }

    private void owner(Question q, Actor a) {
        requireRole(a, Role.USER);
        if (!q.createdBy().equals(a.userId())) throw new ForbiddenException("Question belongs to another user");
    }

    private static void requireRole(Actor a, Role r) {
        if (a.role() != r) throw new ForbiddenException("Role " + r + " is required");
    }

    private static void require(boolean b, String m) {
        if (!b) throw new ForbiddenException(m);
    }

    private static List<QuestionOption> withIds(List<QuestionOption> options) {
        return options.stream().map(o -> new QuestionOption(o.id() == null ? UUID.randomUUID() : o.id(), o.label(), o.content(), o.imageUrl(), o.storageKey(), o.correct(), o.sortOrder())).toList();
    }

    private void deleteImage(String publicId) { if (imageStorage != null) imageStorage.delete(publicId); }

    private void validateTaxonomy(UUID subjectId, UUID chapterId, UUID topicId, UUID knowledgeItemId, String facultyId) {
        if (catalog == null) return;
        var subject = catalog.findSubject(subjectId).orElseThrow(() -> new NotFoundException("Subject not found"));
        if (!subject.isAvailableTo(facultyId))
            throw new ForbiddenException("SUBJECT_ACCESS_DENIED", "Subject is outside faculty scope");
        var chapter = catalog.findChapter(chapterId).orElseThrow(() -> new NotFoundException("Chapter not found"));
        if (!Objects.equals(chapter.subjectId(), subjectId)) throw new IllegalArgumentException("Chapter does not belong to subject");
        if (topicId != null) {
            var topic = catalog.findTopic(topicId).orElseThrow(() -> new NotFoundException("Topic not found"));
            if (!Objects.equals(topic.chapterId(), chapterId)) throw new IllegalArgumentException("Topic does not belong to chapter");
        }
        if (knowledgeItemId != null) {
            var item = catalog.findKnowledgeItem(knowledgeItemId)
                    .orElseThrow(() -> new NotFoundException("KNOWLEDGE_ITEM_NOT_FOUND", "Knowledge item not found"));
            if (topicId == null || !Objects.equals(item.topicId(), topicId))
                throw new IllegalArgumentException("Knowledge item does not belong to topic");
        }
    }

    private void requireAssignment(UUID subjectId, Actor actor) {
        if (actor.role() == Role.USER && assignments != null && !assignments.existsActive(subjectId, actor.userId()))
            throw new ForbiddenException("SUBJECT_NOT_ASSIGNED", "User is not actively assigned to this subject");
    }

    @Transactional(readOnly = true)
    public PageResult<Question> searchApproved(Actor actor, QuestionCriteria c) {
        if (actor == null || actor.role() == Role.SYSTEM_ADMIN) {
            throw new ForbiddenException("Approved question bank is not available to this role");
        }
        // The approved bank is shared within the authenticated faculty. USER-specific
        // ownership filtering belongs to the workflow/my-questions query, not this bank.
        String faculty = actor.facultyId();
        if (faculty == null || faculty.isBlank()) {
            throw new ForbiddenException("Faculty assignment is required");
        }
        return repository.search(new QuestionCriteria(faculty, c.subjectId(), c.chapterId(), c.topicId(), c.difficulty(),
                QuestionStatus.APPROVED, c.source(), null, c.keyword(), c.page(), c.size(), c.sort()));
    }

    private void validateQuestionAssignment(UUID assignmentId, UUID subjectId, UUID chapterId, UUID topicId, UUID knowledgeItemId, Actor actor) {
        if (assignmentId == null) return;
        if (questionAssignments == null) throw new IllegalArgumentException("Question assignment support is unavailable");
        var assignment = questionAssignments.findById(assignmentId).orElseThrow(() -> new NotFoundException("ASSIGNMENT_NOT_FOUND", "Question assignment not found"));
        if (!Objects.equals(assignment.lecturerId(), actor.userId())) throw new ForbiddenException("ASSIGNMENT_ACCESS_DENIED", "Assignment belongs to another lecturer");
        if (!Objects.equals(assignment.subjectId(), subjectId)
                || (assignment.chapterId() != null && !Objects.equals(assignment.chapterId(), chapterId))
                || (assignment.topicId() != null && !Objects.equals(assignment.topicId(), topicId))
                || (assignment.knowledgeItemId() != null && !Objects.equals(assignment.knowledgeItemId(), knowledgeItemId)))
            throw new IllegalArgumentException("Question taxonomy does not match assignment scope");
    }
}
