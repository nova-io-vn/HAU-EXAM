package com.examservice.infrastructure.persistence.adapter;

import com.examservice.application.port.out.ExamMatrixRepository;
import com.examservice.application.port.out.ExamRepository;
import com.examservice.application.port.out.ExamTemplateRepository;
import com.examservice.domain.model.*;
import com.examservice.infrastructure.persistence.entity.*;
import com.examservice.infrastructure.persistence.mapper.TemplateMapper;
import com.examservice.infrastructure.persistence.repository.Exams;
import com.examservice.infrastructure.persistence.repository.Matrices;
import com.examservice.infrastructure.persistence.repository.Templates;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

public final class PersistenceAdapters {
    private PersistenceAdapters() { }

    @Component
    public static class MatrixAdapter implements ExamMatrixRepository {
        private final Matrices repository;

        public MatrixAdapter(Matrices repository) {
            this.repository = repository;
        }

        public ExamMatrix save(ExamMatrix matrix) {
            MatrixEntity entity = toEntity(matrix);
            repository.findById(matrix.id()).ifPresent(existing -> entity.version = existing.version);
            return toDomain(repository.save(entity));
        }

        public Optional<ExamMatrix> findById(UUID id) {
            return repository.findById(id).map(this::toDomain);
        }

        public List<ExamMatrix> findByFaculty(String facultyId) {
            return repository.findAllByFacultyIdOrderByUpdatedAtDesc(facultyId).stream().map(this::toDomain).toList();
        }

        private MatrixEntity toEntity(ExamMatrix matrix) {
            MatrixEntity entity = new MatrixEntity();
            entity.id = matrix.id(); entity.name = matrix.name(); entity.facultyId = matrix.facultyId();
            entity.subjectId = matrix.subjectId(); entity.totalQuestions = matrix.totalQuestions();
            entity.createdBy = matrix.createdBy(); entity.createdAt = matrix.createdAt(); entity.updatedAt = matrix.updatedAt();
            entity.rules = matrix.rules().stream().map(rule -> {
                MatrixRuleEntity value = new MatrixRuleEntity();
                value.id = rule.id() == null ? UUID.randomUUID() : rule.id(); value.matrix = entity;
                value.chapterId = rule.chapterId(); value.topicId = rule.topicId();
                value.knowledgeItemId = rule.knowledgeItemId(); value.difficulty = rule.difficulty();
                value.questionCount = rule.questionCount();
                return value;
            }).toList();
            return entity;
        }

        private ExamMatrix toDomain(MatrixEntity entity) {
            return new ExamMatrix(entity.id, entity.name, entity.facultyId, entity.subjectId, entity.totalQuestions,
                    entity.rules.stream().map(rule -> new ExamMatrixRule(rule.id, rule.chapterId, rule.topicId,
                            rule.knowledgeItemId, rule.difficulty, rule.questionCount)).toList(),
                    entity.createdBy, entity.createdAt, entity.updatedAt);
        }
    }

    @Component
    public static class TemplateAdapter implements ExamTemplateRepository {
        private final Templates repository;
        private final TemplateMapper mapper;

        public TemplateAdapter(Templates repository, TemplateMapper mapper) {
            this.repository = repository; this.mapper = mapper;
        }

        public ExamTemplate save(ExamTemplate template) {
            return mapper.toDomain(repository.save(mapper.toEntity(template)));
        }

        public Optional<ExamTemplate> findById(UUID id) {
            return repository.findById(id).map(mapper::toDomain);
        }
    }

    @Component
    public static class ExamAdapter implements ExamRepository {
        private final Exams repository;

        public ExamAdapter(Exams repository) {
            this.repository = repository;
        }

        public Exam save(Exam exam) {
            ExamEntity entity = toEntity(exam);
            repository.findById(exam.id()).ifPresent(existing -> entity.version = existing.version);
            return toDomain(repository.save(entity));
        }

        public Optional<Exam> findById(UUID id) {
            return repository.findById(id).map(this::toDomain);
        }

        public List<Exam> findByFaculty(String facultyId) {
            return repository.findAllByFacultyIdOrderByUpdatedAtDesc(facultyId).stream().map(this::toDomain).toList();
        }

        private ExamEntity toEntity(Exam exam) {
            ExamEntity entity = new ExamEntity();
            entity.id = exam.id(); entity.name = exam.name(); entity.examCode = exam.examCode();
            entity.durationMinutes = exam.durationMinutes(); entity.status = exam.status();
            entity.facultyId = exam.facultyId(); entity.subjectId = exam.subjectId(); entity.matrixId = exam.matrixId();
            entity.templateId = exam.templateId(); entity.createdBy = exam.createdBy();
            entity.versionStartCode = exam.versionStartCode(); entity.versionEndCode = exam.versionEndCode();
            entity.shuffleQuestions = exam.shuffleQuestions(); entity.shuffleAnswers = exam.shuffleAnswers();
            entity.allowQuestionReplacement = exam.allowQuestionReplacement(); entity.reuseWarning = exam.reuseWarning();
            entity.createdAt = exam.createdAt(); entity.updatedAt = exam.updatedAt();
            entity.versions = exam.versions().stream().map(version -> versionEntity(entity, version)).toList();
            return entity;
        }

        private ExamVersionEntity versionEntity(ExamEntity exam, ExamVersion version) {
            ExamVersionEntity entity = new ExamVersionEntity();
            entity.id = version.id(); entity.exam = exam; entity.versionNumber = version.versionCode();
            entity.createdBy = version.createdBy(); entity.generationSeed = version.generationSeed();
            entity.questionCount = version.questionCount(); entity.createdAt = version.createdAt();
            entity.questions = version.questions().stream().map(question -> questionEntity(entity, question)).toList();
            return entity;
        }

        private ExamQuestionEntity questionEntity(ExamVersionEntity version, ExamQuestionReference question) {
            ExamQuestionEntity entity = new ExamQuestionEntity();
            entity.id = question.id(); entity.version = version; entity.questionId = question.questionId();
            entity.position = question.position(); entity.matrixRuleId = question.matrixRuleId();
            entity.options = question.options().stream().map(option -> {
                ExamOptionEntity value = new ExamOptionEntity();
                value.id = option.id(); value.question = entity; value.optionId = option.optionId();
                value.displayOrder = option.displayOrder();
                return value;
            }).toList();
            return entity;
        }

        private Exam toDomain(ExamEntity entity) {
            return new Exam(entity.id, entity.name, entity.examCode, entity.durationMinutes, entity.status,
                    entity.facultyId, entity.subjectId, entity.matrixId, entity.templateId, entity.createdBy,
                    entity.versionStartCode, entity.versionEndCode, entity.shuffleQuestions, entity.shuffleAnswers,
                    entity.allowQuestionReplacement, entity.reuseWarning,
                    entity.versions.stream().map(version -> new ExamVersion(version.id, version.versionNumber,
                            version.generationSeed, version.createdBy,
                            version.questions.stream().map(question -> new ExamQuestionReference(question.id,
                                    question.questionId(), question.position(), question.matrixRuleId(),
                                    question.options.stream().map(option -> new ExamOptionReference(option.id,
                                            option.optionId, option.displayOrder)).toList())).toList(),
                            version.createdAt)).toList(), entity.createdAt, entity.updatedAt);
        }
    }
}
