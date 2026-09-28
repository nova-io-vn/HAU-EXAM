package com.questionservice.application.service;

import com.questionservice.application.model.LecturerProfile;
import com.questionservice.application.port.out.CatalogRepository;
import com.questionservice.application.port.out.SubjectAssignmentRepository;
import com.questionservice.application.port.out.UserDirectoryPort;
import com.questionservice.domain.exception.ForbiddenException;
import com.questionservice.domain.exception.NotFoundException;
import com.questionservice.domain.model.Actor;
import com.questionservice.domain.model.Chapter;
import com.questionservice.domain.model.Role;
import com.questionservice.domain.model.Subject;
import com.questionservice.domain.model.Topic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class CatalogService {
    private static final Logger log = LoggerFactory.getLogger(CatalogService.class);
    private final CatalogRepository repo;
    private final Clock clock;
    private final SubjectAssignmentRepository assignments;
    private final UserDirectoryPort users;

    public CatalogService(CatalogRepository repo, Clock clock, SubjectAssignmentRepository assignments,
                          UserDirectoryPort users) {
        this.repo = repo;
        this.clock = clock;
        this.assignments = assignments;
        this.users = users;
    }

    public Subject saveSubject(UUID id, String code, String name, String requestedManagingFaculty,
                               Set<String> requestedParticipatingFaculties, Actor actor) {
        Instant now = Instant.now(clock);
        Subject old = id == null ? null : subject(id);
        if (old != null) manageCanonical(actor, old);

        String managingFaculty;
        Set<String> participatingFaculties;
        if (actor.role() == Role.SYSTEM_ADMIN) {
            managingFaculty = required(requestedManagingFaculty != null ? requestedManagingFaculty
                    : old == null ? null : old.managingFacultyId());
            participatingFaculties = normalizedScopes(managingFaculty, requestedParticipatingFaculties,
                    old == null ? Set.of() : old.participatingFacultyIds());
        } else {
            managingFaculty = old == null ? requiredFaculty(actor) : old.managingFacultyId();
            participatingFaculties = old == null ? Set.of(managingFaculty) : old.participatingFacultyIds();
        }
        return repo.saveSubject(new Subject(id == null ? UUID.randomUUID() : id, managingFaculty,
                participatingFaculties, required(code), required(name), old == null ? now : old.createdAt(), now));
    }

    public Chapter saveChapter(UUID id, UUID subjectId, String code, String name, int ordinal, Actor actor) {
        Subject subject = subject(subjectId);
        manageCanonical(actor, subject);
        if (id != null) {
            Chapter existing = chapter(id);
            if (!existing.subjectId().equals(subjectId)) throw new IllegalArgumentException("Chapter does not belong to subject");
        }
        Instant now = Instant.now(clock);
        Chapter old = id == null ? null : chapter(id);
        return repo.saveChapter(new Chapter(id == null ? UUID.randomUUID() : id, subjectId, required(code),
                required(name), ordinal, old == null ? now : old.createdAt(), now));
    }

    public Topic saveTopic(UUID id, UUID chapterId, String code, String name, Actor actor) {
        Chapter chapter = chapter(chapterId);
        Subject subject = subject(chapter.subjectId());
        manageCanonical(actor, subject);
        if (id != null) {
            Topic existing = topic(id);
            if (!existing.chapterId().equals(chapterId)) throw new IllegalArgumentException("Topic does not belong to chapter");
        }
        Instant now = Instant.now(clock);
        Topic old = id == null ? null : topic(id);
        return repo.saveTopic(new Topic(id == null ? UUID.randomUUID() : id, chapterId, required(code),
                required(name), old == null ? now : old.createdAt(), now));
    }

    @Transactional(readOnly = true)
    public List<Subject> subjects(Actor actor, String facultyId) {
        if (actor.role() == Role.USER) {
            Set<UUID> ids = new LinkedHashSet<>(assignments.findActiveSubjectIds(actor.userId()));
            return repo.findSubjects(null).stream().filter(subject -> ids.contains(subject.id())).toList();
        }
        return repo.findSubjects(actor.role() == Role.SUBJECT_ADMIN ? requiredFaculty(actor) : facultyId);
    }

    public void assignLecturer(UUID subjectId, UUID userId, Actor actor) {
        try {
            Subject subject = subject(subjectId);
            manageCanonical(actor, subject);
            if (userId == null || actor.userId() == null) throw new ForbiddenException("SUBJECT_ACCESS_DENIED", "Authenticated user is required");
            LecturerProfile lecturer = users.findLecturer(userId)
                    .orElseThrow(() -> new NotFoundException("LECTURER_NOT_FOUND", "Lecturer was not found"));
            if (!"USER".equals(lecturer.role()) || !"ACTIVE".equals(lecturer.status()))
                throw new NotFoundException("LECTURER_NOT_FOUND", "Active lecturer was not found");
            if (!subject.isAvailableTo(lecturer.facultyId()))
                throw new ForbiddenException("LECTURER_OUTSIDE_SCOPE", "Lecturer faculty is outside the subject scope");
            assignments.assign(subjectId, userId, actor.userId(), Instant.now(clock));
            log.info("method=POST path=/api/v1/subjects/{}/lecturers role={} facultyId={} subjectId={} targetUserId={} decision=ALLOW errorCode=SUCCESS",
                    subjectId, actor.role(), actor.facultyId(), subjectId, userId);
        } catch (ForbiddenException exception) {
            log.warn("method=POST path=/api/v1/subjects/{}/lecturers role={} facultyId={} subjectId={} targetUserId={} decision=DENY errorCode={}",
                    subjectId, actor.role(), actor.facultyId(), subjectId, userId, exception.code());
            throw exception;
        } catch (NotFoundException exception) {
            log.warn("method=POST path=/api/v1/subjects/{}/lecturers role={} facultyId={} subjectId={} targetUserId={} decision=DENY errorCode={}",
                    subjectId, actor.role(), actor.facultyId(), subjectId, userId, exception.code());
            throw exception;
        }
    }

    public void removeLecturer(UUID subjectId, UUID userId, Actor actor) {
        Subject subject = subject(subjectId);
        manageCanonical(actor, subject);
        assignments.remove(subjectId, userId);
    }

    @Transactional(readOnly = true)
    public List<LecturerProfile> lecturers(UUID subjectId, Actor actor) {
        Subject subject = subject(subjectId);
        requireReadScope(actor, subject);
        return assignments.findActiveUserIds(subjectId).stream()
                .map(users::findLecturer)
                .flatMap(java.util.Optional::stream)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LecturerProfile> eligibleLecturers(UUID subjectId, String keyword, Actor actor) {
        Subject subject = subject(subjectId);
        manageCanonical(actor, subject);
        Set<UUID> assigned = new LinkedHashSet<>(assignments.findActiveUserIds(subjectId));
        return users.findActiveLecturers(subject.participatingFacultyIds(), keyword).stream()
                .filter(lecturer -> !assigned.contains(lecturer.userId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isAssigned(UUID subjectId, UUID userId) {
        return assignments.existsActive(subjectId, userId);
    }

    @Transactional(readOnly = true)
    public CatalogContext context(UUID subjectId, UUID chapterId, UUID topicId) {
        Subject subject = subject(subjectId);
        Chapter chapter = chapterId == null ? null : chapter(chapterId);
        if (chapter != null && !subject.id().equals(chapter.subjectId()))
            throw new IllegalArgumentException("Chapter does not belong to subject");
        Topic topic = topicId == null ? null : topic(topicId);
        if (topic != null && (chapter == null || !chapter.id().equals(topic.chapterId())))
            throw new IllegalArgumentException("Topic does not belong to chapter");
        return new CatalogContext(subject.id(), subject.code(), subject.name(), subject.managingFacultyId(),
                chapter == null ? null : chapter.id(), chapter == null ? null : chapter.code(), chapter == null ? null : chapter.name(),
                topic == null ? null : topic.id(), topic == null ? null : topic.code(), topic == null ? null : topic.name());
    }

    public record CatalogContext(UUID subjectId, String subjectCode, String subjectName, String managingFacultyId,
                                 UUID chapterId, String chapterCode, String chapterName,
                                 UUID topicId, String topicCode, String topicName) { }

    @Transactional(readOnly = true)
    public List<Chapter> chapters(UUID subjectId, Actor actor) {
        Subject subject = subject(subjectId);
        requireReadScope(actor, subject);
        return repo.findChapters(subjectId);
    }

    @Transactional(readOnly = true)
    public List<Topic> topics(UUID chapterId, Actor actor) {
        Chapter chapter = chapter(chapterId);
        Subject subject = subject(chapter.subjectId());
        requireReadScope(actor, subject);
        return repo.findTopics(chapterId);
    }

    public void deleteSubject(UUID id, Actor actor) {
        Subject subject = subject(id);
        manageCanonical(actor, subject);
        repo.deleteSubject(id);
    }

    public void deleteChapter(UUID id, Actor actor) {
        Chapter chapter = chapter(id);
        manageCanonical(actor, subject(chapter.subjectId()));
        repo.deleteChapter(id);
    }

    public void deleteTopic(UUID id, Actor actor) {
        Topic topic = topic(id);
        Chapter chapter = chapter(topic.chapterId());
        manageCanonical(actor, subject(chapter.subjectId()));
        repo.deleteTopic(id);
    }

    private Subject subject(UUID id) {
        return repo.findSubject(id).orElseThrow(() -> new NotFoundException("SUBJECT_NOT_FOUND", "Subject not found"));
    }
    private Chapter chapter(UUID id) {
        return repo.findChapter(id).orElseThrow(() -> new NotFoundException("CHAPTER_NOT_FOUND", "Chapter not found"));
    }
    private Topic topic(UUID id) {
        return repo.findTopic(id).orElseThrow(() -> new NotFoundException("TOPIC_NOT_FOUND", "Topic not found"));
    }

    private void manageCanonical(Actor actor, Subject subject) {
        if (actor.role() == Role.SYSTEM_ADMIN) return;
        if (actor.role() != Role.SUBJECT_ADMIN || !Objects.equals(actor.facultyId(), subject.managingFacultyId()))
            throw new ForbiddenException("SUBJECT_ACCESS_DENIED", "Only the managing faculty may change the canonical subject structure");
    }

    private void requireReadScope(Actor actor, Subject subject) {
        if (actor.role() == Role.SYSTEM_ADMIN) return;
        if (actor.role() == Role.USER) {
            if (actor.userId() != null && assignments.existsActive(subject.id(), actor.userId())) return;
        } else if (actor.role() == Role.SUBJECT_ADMIN && subject.isAvailableTo(actor.facultyId())) {
            return;
        }
        throw new ForbiddenException("SUBJECT_ACCESS_DENIED", "Subject is outside the authenticated faculty scope");
    }

    private static Set<String> normalizedScopes(String managingFaculty, Set<String> requested, Set<String> fallback) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        result.add(managingFaculty);
        Set<String> source = requested == null ? fallback : requested;
        if (source != null) source.stream().filter(value -> value != null && !value.isBlank()).map(String::trim).forEach(result::add);
        return result;
    }
    private static String requiredFaculty(Actor actor) {
        if (actor == null || actor.facultyId() == null || actor.facultyId().isBlank())
            throw new ForbiddenException("SUBJECT_ACCESS_DENIED", "A faculty assignment is required");
        return actor.facultyId();
    }
    private static String required(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Value is required");
        return value.trim();
    }
}
