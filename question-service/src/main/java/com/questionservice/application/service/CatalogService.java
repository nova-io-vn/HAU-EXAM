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

    public CatalogService(CatalogRepository r, Clock c) {
        repo = r;
        clock = c;
    }

    public Subject saveSubject(UUID id, String faculty, String code, String name, Actor a) {
        catalogAdmin(a, faculty);
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
        return repo.findSubjects(a.role() == Role.SUBJECT_ADMIN ? a.facultyId() : f);
    }

    @Transactional(readOnly = true)
    public List<Chapter> chapters(UUID id) {
        return repo.findChapters(id);
    }

    @Transactional(readOnly = true)
    public List<Topic> topics(UUID id) {
        return repo.findTopics(id);
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
        if (a.role() != Role.SUBJECT_ADMIN || a.facultyId() == null || !a.facultyId().equals(f))
            throw new ForbiddenException("Catalog is outside administrator faculty scope");
    }

    private static String required(String v) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException("Value is required");
        return v.trim();
    }
}
