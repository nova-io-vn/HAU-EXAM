package com.questionservice.infrastructure.persistence.adapter;

import com.questionservice.application.port.out.CatalogRepository;
import com.questionservice.domain.model.Chapter;
import com.questionservice.domain.model.Subject;
import com.questionservice.domain.model.Topic;
import com.questionservice.infrastructure.persistence.entity.SubjectEntity;
import com.questionservice.infrastructure.persistence.entity.SubjectFacultyScopeEntity;
import com.questionservice.infrastructure.persistence.mapper.CatalogMapper;
import com.questionservice.infrastructure.persistence.repository.ChapterJpaRepository;
import com.questionservice.infrastructure.persistence.repository.SubjectFacultyScopeJpaRepository;
import com.questionservice.infrastructure.persistence.repository.SubjectJpaRepository;
import com.questionservice.infrastructure.persistence.repository.TopicJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CatalogPersistenceAdapter implements CatalogRepository {
    private final SubjectJpaRepository subjects;
    private final SubjectFacultyScopeJpaRepository scopes;
    private final ChapterJpaRepository chapters;
    private final TopicJpaRepository topics;
    private final CatalogMapper mapper;

    public CatalogPersistenceAdapter(SubjectJpaRepository subjects, SubjectFacultyScopeJpaRepository scopes,
                                     ChapterJpaRepository chapters, TopicJpaRepository topics, CatalogMapper mapper) {
        this.subjects = subjects;
        this.scopes = scopes;
        this.chapters = chapters;
        this.topics = topics;
        this.mapper = mapper;
    }

    @Override
    public Subject saveSubject(Subject value) {
        SubjectEntity entity = new SubjectEntity();
        entity.id = value.id();
        entity.managingFacultyId = value.managingFacultyId();
        entity.code = value.code();
        entity.name = value.name();
        entity.createdAt = value.createdAt();
        entity.updatedAt = value.updatedAt();
        SubjectEntity saved = subjects.save(entity);

        var existing = new java.util.ArrayList<>(scopes.findAllBySubjectId(saved.id));
        existing.forEach(scope -> scope.active = value.participatingFacultyIds().contains(scope.facultyId));
        for (String facultyId : value.participatingFacultyIds()) {
            if (existing.stream().noneMatch(scope -> scope.facultyId.equals(facultyId))) {
                SubjectFacultyScopeEntity scope = new SubjectFacultyScopeEntity();
                scope.id = UUID.randomUUID();
                scope.subjectId = saved.id;
                scope.facultyId = facultyId;
                scope.active = true;
                existing.add(scope);
            }
        }
        scopes.saveAll(existing);
        return toDomain(saved);
    }

    @Override
    public Optional<Subject> findSubject(UUID id) {
        return subjects.findById(id).map(this::toDomain);
    }

    @Override
    public List<Subject> findSubjects(String facultyId) {
        if (facultyId == null || facultyId.isBlank()) {
            return subjects.findAllByOrderByCode().stream().map(this::toDomain).toList();
        }
        Set<UUID> subjectIds = scopes.findAllByFacultyIdAndActiveTrue(facultyId.trim()).stream()
                .map(scope -> scope.subjectId)
                .collect(Collectors.toSet());
        return subjects.findAllById(subjectIds).stream()
                .sorted(Comparator.comparing(subject -> subject.code))
                .map(this::toDomain)
                .toList();
    }

    private Subject toDomain(SubjectEntity entity) {
        Set<String> facultyIds = scopes.findAllBySubjectIdAndActiveTrue(entity.id).stream()
                .map(scope -> scope.facultyId)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
        return new Subject(entity.id, entity.managingFacultyId, facultyIds, entity.code, entity.name,
                entity.createdAt, entity.updatedAt);
    }

    @Override
    public void deleteSubject(UUID id) { subjects.deleteById(id); }
    @Override
    public Chapter saveChapter(Chapter value) { return mapper.toDomain(chapters.save(mapper.toEntity(value))); }
    @Override
    public Optional<Chapter> findChapter(UUID id) { return chapters.findById(id).map(mapper::toDomain); }
    @Override
    public List<Chapter> findChapters(UUID id) { return chapters.findAllBySubjectIdOrderByOrdinal(id).stream().map(mapper::toDomain).toList(); }
    @Override
    public void deleteChapter(UUID id) { chapters.deleteById(id); }
    @Override
    public Topic saveTopic(Topic value) { return mapper.toDomain(topics.save(mapper.toEntity(value))); }
    @Override
    public Optional<Topic> findTopic(UUID id) { return topics.findById(id).map(mapper::toDomain); }
    @Override
    public List<Topic> findTopics(UUID id) { return topics.findAllByChapterIdOrderByCode(id).stream().map(mapper::toDomain).toList(); }
    @Override
    public void deleteTopic(UUID id) { topics.deleteById(id); }
}
