package com.questionservice.application.service;

import com.questionservice.application.port.out.CatalogRepository;
import com.questionservice.domain.exception.*;
import com.questionservice.domain.model.*;

import java.time.*;
import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CatalogService {
    private final CatalogRepository repo;
    private final Clock clock;
    private final com.questionservice.application.port.out.SubjectAssignmentRepository assignments;
    private final com.questionservice.application.port.out.UserDirectoryPort users;

    public CatalogService(CatalogRepository r, Clock c) { this(r, c, null, null); }
    public CatalogService(CatalogRepository r, Clock c, com.questionservice.application.port.out.SubjectAssignmentRepository assignments) { this(r, c, assignments, null); }
    @org.springframework.beans.factory.annotation.Autowired
    public CatalogService(CatalogRepository r, Clock c, com.questionservice.application.port.out.SubjectAssignmentRepository assignments, com.questionservice.application.port.out.UserDirectoryPort users) {
        repo = r;
        clock = c;
        this.assignments = assignments;
        this.users = users;
    }

    public Subject saveSubject(UUID id, String code, String name, Actor a) {
        String faculty = requiredFaculty(a);
        if (id != null) { Subject existing = repo.findSubject(id).orElseThrow(() -> new NotFoundException("Subject not found")); catalogAdmin(a, existing.facultyId()); }
        Instant now = Instant.now(clock);
        Subject old = id == null ? null : repo.findSubject(id).orElseThrow(() -> new NotFoundException("Subject not found"));
        return repo.saveSubject(new Subject(id == null ? UUID.randomUUID() : id, faculty, required(code), required(name), old == null ? now : old.createdAt(), now));
    }

    public Chapter saveChapter(UUID id, UUID subjectId, String code, String name, int ordinal, Actor a) {
        Subject s = repo.findSubject(subjectId).orElseThrow(() -> new NotFoundException("Subject not found"));
        catalogAdmin(a, s.facultyId());
        Instant now = Instant.now(clock);
        Chapter old = id == null ? null : repo.findChapter(id).orElseThrow(() -> new NotFoundException("Chapter not found"));
        return repo.saveChapter(new Chapter(id == null ? UUID.randomUUID() : id, subjectId, required(code), required(name), ordinal, old == null ? now : old.createdAt(), now));
    }

    public Topic saveTopic(UUID id, UUID chapterId, String code, String name, Actor a) {
        Chapter c = repo.findChapter(chapterId).orElseThrow(() -> new NotFoundException("Chapter not found"));
        Subject s = repo.findSubject(c.subjectId()).orElseThrow(() -> new NotFoundException("Subject not found"));
        catalogAdmin(a, s.facultyId());
        Instant now = Instant.now(clock);
        Topic old = id == null ? null : repo.findTopic(id).orElseThrow(() -> new NotFoundException("Topic not found"));
        return repo.saveTopic(new Topic(id == null ? UUID.randomUUID() : id, chapterId, required(code), required(name), old == null ? now : old.createdAt(), now));
    }

    @Transactional(readOnly = true)
    public List<Subject> subjects(Actor a, String f) {
        if (a.role() == Role.USER && assignments != null) {
            var ids = assignments.findActiveSubjectIds(a.userId());
            return repo.findSubjects(a.facultyId()).stream().filter(s -> ids.contains(s.id())).toList();
        }
        return repo.findSubjects(a.role() == Role.SUBJECT_ADMIN ? a.facultyId() : f);
    }

    public void assignLecturer(UUID subjectId, UUID userId, Actor actor) {
        Subject subject = repo.findSubject(subjectId).orElseThrow(() -> new NotFoundException("Subject not found"));
        catalogAdmin(actor, subject.facultyId());
        if (userId == null || actor.userId() == null) throw new ForbiddenException("Authenticated user is required");
        if (users != null && !users.isLecturerInFaculty(userId, subject.facultyId())) throw new ForbiddenException("Lecturer is outside subject faculty");
        assignments.assign(subjectId, userId, actor.userId(), Instant.now(clock));
    }
    public void removeLecturer(UUID subjectId, UUID userId, Actor actor) {
        Subject subject = repo.findSubject(subjectId).orElseThrow(() -> new NotFoundException("Subject not found"));
        catalogAdmin(actor, subject.facultyId());
        assignments.remove(subjectId, userId);
    }
    @Transactional(readOnly = true)
    public List<UUID> lecturers(UUID subjectId, Actor actor) {
        Subject subject = repo.findSubject(subjectId).orElseThrow(() -> new NotFoundException("Subject not found"));
        requireReadScope(actor, subject.facultyId());
        return assignments.findActiveUserIds(subjectId);
    }
    @Transactional(readOnly = true)
    public boolean isAssigned(UUID subjectId, UUID userId) { return assignments.existsActive(subjectId, userId); }

    @Transactional(readOnly = true)
    public List<Chapter> chapters(UUID id, Actor a) {
        Subject s=repo.findSubject(id).orElseThrow(()->new NotFoundException("Subject not found"));
        requireReadScope(a,s.facultyId()); return repo.findChapters(id);
    }

    @Transactional(readOnly = true)
    public List<Topic> topics(UUID id, Actor a) {
        Chapter c=repo.findChapter(id).orElseThrow(()->new NotFoundException("Chapter not found"));
        Subject s=repo.findSubject(c.subjectId()).orElseThrow(()->new NotFoundException("Subject not found"));
        requireReadScope(a,s.facultyId()); return repo.findTopics(id);
    }

    public void deleteSubject(UUID id, Actor a) {
        Subject s = repo.findSubject(id).orElseThrow(() -> new NotFoundException("Subject not found"));
        catalogAdmin(a, s.facultyId());
        repo.deleteSubject(id);
    }

    public void deleteChapter(UUID id, Actor a) {
        Chapter c = repo.findChapter(id).orElseThrow(() -> new NotFoundException("Chapter not found"));
        Subject s = repo.findSubject(c.subjectId()).orElseThrow(() -> new NotFoundException("Subject not found"));
        catalogAdmin(a, s.facultyId());
        repo.deleteChapter(id);
    }

    public void deleteTopic(UUID id, Actor a) {
        Topic t = repo.findTopic(id).orElseThrow(() -> new NotFoundException("Topic not found"));
        Chapter c = repo.findChapter(t.chapterId()).orElseThrow(() -> new NotFoundException("Chapter not found"));
        Subject s = repo.findSubject(c.subjectId()).orElseThrow(() -> new NotFoundException("Subject not found"));
        catalogAdmin(a, s.facultyId());
        repo.deleteTopic(id);
    }

    private static void catalogAdmin(Actor a, String f) {
        if (a.role() == Role.SYSTEM_ADMIN) return;
        if (a.role() != Role.SUBJECT_ADMIN || a.facultyId() == null || !a.facultyId().equals(f))
            throw new ForbiddenException("Catalog is outside administrator faculty scope");
    }
    private static String requiredFaculty(Actor a) { if (a == null || a.facultyId() == null || a.facultyId().isBlank()) throw new ForbiddenException("A faculty assignment is required"); return a.facultyId(); }
    private static void requireReadScope(Actor a,String faculty){if(a==null || a.role()==Role.SYSTEM_ADMIN) return; if(a.facultyId()==null||!a.facultyId().equals(faculty))throw new ForbiddenException("Catalog is outside faculty scope");}

    private static String required(String v) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException("Value is required");
        return v.trim();
    }
}
