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

    public QuestionService(QuestionRepository repository, QuestionEventPublisher publisher, Clock clock) {
        this(repository, publisher, clock, null);
    }
    @org.springframework.beans.factory.annotation.Autowired
    public QuestionService(QuestionRepository repository, QuestionEventPublisher publisher, Clock clock, CatalogRepository catalog) {
        this.repository = repository; this.publisher = publisher; this.clock = clock; this.catalog = catalog;
    }

    public Question create(Actor actor, QuestionInput in) {
        requireRole(actor, Role.USER);
        require(actor.userId() != null, "Authenticated user is required");
        if (actor.facultyId() == null || actor.facultyId().isBlank())
            throw new ForbiddenException("Question creator must have a faculty assignment");
        validateTaxonomy(in.subjectId(), in.chapterId(), in.topicId(), actor.facultyId());
        var q = Question.create(UUID.randomUUID(), actor.facultyId(), in.subjectId(), in.chapterId(), in.topicId(), in.content(), in.imageUrl(), in.storageKey(), in.type(), in.difficulty(), QuestionSource.MANUAL, null, actor.userId(), withIds(in.options()), Instant.now(clock));
        return repository.save(q);
    }

    public Question update(UUID id, Actor actor, QuestionInput in) {
        var q = get(id);
        owner(q, actor);
        validateTaxonomy(in.subjectId(), in.chapterId(), in.topicId(), actor.facultyId());
        q.edit(in.content(), in.imageUrl(), in.storageKey(), in.type(), in.difficulty(), withIds(in.options()), Instant.now(clock));
        return repository.save(q);
    }

    public void delete(UUID id, Actor actor) {
        var q = get(id);
        owner(q, actor);
        if (q.status() != QuestionStatus.DRAFT) throw new InvalidTransitionException("Only DRAFT can be deleted");
        repository.delete(id);
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

    private void validateTaxonomy(UUID subjectId, UUID chapterId, UUID topicId, String facultyId) {
        if (catalog == null) return;
        var subject = catalog.findSubject(subjectId).orElseThrow(() -> new NotFoundException("Subject not found"));
        if (!Objects.equals(subject.facultyId(), facultyId)) throw new ForbiddenException("Subject is outside faculty scope");
        var chapter = catalog.findChapter(chapterId).orElseThrow(() -> new NotFoundException("Chapter not found"));
        if (!Objects.equals(chapter.subjectId(), subjectId)) throw new IllegalArgumentException("Chapter does not belong to subject");
        if (topicId != null) {
            var topic = catalog.findTopic(topicId).orElseThrow(() -> new NotFoundException("Topic not found"));
            if (!Objects.equals(topic.chapterId(), chapterId)) throw new IllegalArgumentException("Topic does not belong to chapter");
        }
    }
}
