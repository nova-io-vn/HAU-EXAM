package com.questionservice.infrastructure.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.questionservice.domain.model.Difficulty;
import com.questionservice.domain.model.QuestionSource;
import com.questionservice.domain.model.QuestionStatus;
import com.questionservice.domain.model.QuestionType;
import com.questionservice.infrastructure.persistence.entity.ChapterEntity;
import com.questionservice.infrastructure.persistence.entity.KnowledgeItemEntity;
import com.questionservice.infrastructure.persistence.entity.QuestionEntity;
import com.questionservice.infrastructure.persistence.entity.SubjectEntity;
import com.questionservice.infrastructure.persistence.entity.TopicEntity;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class QuestionCoverageRepositoryTest {
    private static final Instant NOW = Instant.parse("2026-09-29T00:00:00Z");

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private QuestionJpaRepository repository;

    @Test
    void aggregatesOnlyApprovedMappedQuestionsAndCountsLegacyRowsSeparately() {
        UUID subjectId = UUID.randomUUID();
        UUID chapterId = UUID.randomUUID();
        UUID topicId = UUID.randomUUID();
        UUID k1 = UUID.randomUUID();
        UUID k2 = UUID.randomUUID();
        UUID k3 = UUID.randomUUID();
        persistTaxonomy(subjectId, chapterId, topicId, List.of(k1, k2, k3));

        persistQuestion(subjectId, chapterId, topicId, k1, QuestionStatus.APPROVED, Difficulty.EASY);
        persistQuestion(subjectId, chapterId, topicId, k1, QuestionStatus.APPROVED, Difficulty.EASY);
        persistQuestion(subjectId, chapterId, topicId, k3, QuestionStatus.APPROVED, Difficulty.HARD);
        persistQuestion(subjectId, chapterId, topicId, k2, QuestionStatus.DRAFT, Difficulty.EASY);
        persistQuestion(subjectId, chapterId, topicId, k2, QuestionStatus.PENDING_REVIEW, Difficulty.MEDIUM);
        persistQuestion(subjectId, chapterId, topicId, k2, QuestionStatus.NEED_REVISION, Difficulty.HARD);
        persistQuestion(subjectId, chapterId, topicId, k2, QuestionStatus.REJECTED, Difficulty.EASY);
        persistQuestion(subjectId, chapterId, topicId, null, QuestionStatus.APPROVED, Difficulty.MEDIUM);
        persistQuestion(subjectId, chapterId, topicId, null, QuestionStatus.PENDING_REVIEW, Difficulty.EASY);
        entityManager.flush();

        Map<String, Long> counts = repository.approvedCoverage(subjectId).stream().collect(Collectors.toMap(
                row -> row[0] + ":" + row[1], row -> ((Number) row[2]).longValue()));

        assertEquals(Map.of(k1 + ":EASY", 2L, k3 + ":HARD", 1L), counts);
        assertEquals(2, repository.countBySubjectIdAndKnowledgeItemIdIsNull(subjectId));
    }

    private void persistTaxonomy(UUID subjectId, UUID chapterId, UUID topicId, List<UUID> itemIds) {
        SubjectEntity subject = new SubjectEntity();
        subject.id = subjectId; subject.managingFacultyId = "CNTT"; subject.code = "S1";
        subject.name = "Subject 1"; subject.createdAt = NOW; subject.updatedAt = NOW;
        entityManager.persist(subject);

        ChapterEntity chapter = new ChapterEntity();
        chapter.id = chapterId; chapter.subjectId = subjectId; chapter.code = "C1";
        chapter.name = "Chapter 1"; chapter.ordinal = 1; chapter.createdAt = NOW; chapter.updatedAt = NOW;
        entityManager.persist(chapter);

        TopicEntity topic = new TopicEntity();
        topic.id = topicId; topic.chapterId = chapterId; topic.code = "T1";
        topic.name = "Topic 1"; topic.createdAt = NOW; topic.updatedAt = NOW;
        entityManager.persist(topic);

        int ordinal = 1;
        for (UUID itemId : itemIds) {
            KnowledgeItemEntity item = new KnowledgeItemEntity();
            item.id = itemId; item.topicId = topicId; item.code = "K" + ordinal;
            item.name = "Knowledge " + ordinal; item.ordinal = ordinal++;
            item.createdAt = NOW; item.updatedAt = NOW;
            entityManager.persist(item);
        }
    }

    private void persistQuestion(UUID subjectId, UUID chapterId, UUID topicId, UUID knowledgeItemId,
                                 QuestionStatus status, Difficulty difficulty) {
        QuestionEntity question = new QuestionEntity();
        question.id = UUID.randomUUID(); question.facultyId = "CNTT"; question.subjectId = subjectId;
        question.chapterId = chapterId; question.topicId = topicId; question.knowledgeItemId = knowledgeItemId;
        question.content = "Question " + question.id; question.type = QuestionType.SINGLE_CHOICE;
        question.difficulty = difficulty; question.status = status; question.source = QuestionSource.MANUAL;
        question.createdBy = UUID.randomUUID(); question.createdAt = NOW; question.updatedAt = NOW;
        entityManager.persist(question);
    }
}
