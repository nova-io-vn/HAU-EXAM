package com.questionservice.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.questionservice.application.model.CoverageCount;
import com.questionservice.application.port.out.CatalogRepository;
import com.questionservice.application.port.out.QuestionRepository;
import com.questionservice.domain.exception.ForbiddenException;
import com.questionservice.domain.model.Actor;
import com.questionservice.domain.model.Chapter;
import com.questionservice.domain.model.Difficulty;
import com.questionservice.domain.model.KnowledgeItem;
import com.questionservice.domain.model.Role;
import com.questionservice.domain.model.Subject;
import com.questionservice.domain.model.Topic;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CoverageServiceTest {
    private static final Instant NOW = Instant.parse("2026-09-29T00:00:00Z");

    private final CatalogRepository catalog = mock(CatalogRepository.class);
    private final QuestionRepository questions = mock(QuestionRepository.class);
    private final CoverageService service = new CoverageService(catalog, questions);
    private final UUID subjectId = UUID.randomUUID();
    private final UUID chapter1Id = UUID.randomUUID();
    private final UUID chapter2Id = UUID.randomUUID();
    private final UUID topic1Id = UUID.randomUUID();
    private final UUID topic2Id = UUID.randomUUID();
    private final UUID k1Id = UUID.randomUUID();
    private final UUID k2Id = UUID.randomUUID();
    private final UUID k3Id = UUID.randomUUID();
    private final UUID k4Id = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(catalog.findSubject(subjectId)).thenReturn(Optional.of(new Subject(subjectId, "CNTT",
                Set.of("CNTT", "KT"), "S1", "Subject 1", NOW, NOW)));
        when(catalog.findChapters(subjectId)).thenReturn(List.of(
                new Chapter(chapter1Id, subjectId, "C1", "Chapter 1", 1, NOW, NOW),
                new Chapter(chapter2Id, subjectId, "C2", "Chapter 2", 2, NOW, NOW)));
        when(catalog.findTopics(chapter1Id)).thenReturn(List.of(new Topic(topic1Id, chapter1Id, "T1", "Topic 1", NOW, NOW)));
        when(catalog.findTopics(chapter2Id)).thenReturn(List.of(new Topic(topic2Id, chapter2Id, "T2", "Topic 2", NOW, NOW)));
        when(catalog.findKnowledgeItems(topic1Id)).thenReturn(List.of(
                item(k1Id, topic1Id, "K1", 1, 1, 1, 0),
                item(k2Id, topic1Id, "K2", 2, 1, 0, 0)));
        when(catalog.findKnowledgeItems(topic2Id)).thenReturn(List.of(
                item(k3Id, topic2Id, "K3", 1, 0, 0, 1),
                item(k4Id, topic2Id, "K4", 2, 0, 0, 0)));
        when(questions.approvedCoverage(subjectId)).thenReturn(List.of(
                new CoverageCount(k1Id, Difficulty.EASY, 1),
                new CoverageCount(k1Id, Difficulty.MEDIUM, 1),
                new CoverageCount(k3Id, Difficulty.HARD, 1)));
        when(questions.countWithoutKnowledgeItem(subjectId)).thenReturn(1L);
    }

    @Test
    void calculatesKnowledgeCoverageFromDistinctItemsAndKeepsDifficultyTargetsSeparate() {
        var result = service.calculate(subjectId, actor(Role.SUBJECT_ADMIN, "CNTT"));

        assertEquals(4, result.overall().totalKnowledgeItems());
        assertEquals(2, result.overall().coveredKnowledgeItems());
        assertEquals(50.0, result.overall().coveragePercentage());
        assertEquals(3, result.overall().approvedQuestionCount());
        assertEquals(2, result.missingKnowledge().size());
        assertEquals(Set.of(k2Id, k4Id), result.missingKnowledge().stream()
                .map(CoverageService.MissingKnowledge::knowledgeItemId).collect(java.util.stream.Collectors.toSet()));
        assertEquals(CoverageService.KnowledgeCoverageStatus.COVERED,
                result.chapters().get(0).topics().get(0).knowledgeItems().get(0).coverageStatus());
        assertEquals(CoverageService.KnowledgeCoverageStatus.MISSING,
                result.chapters().get(0).topics().get(0).knowledgeItems().get(1).coverageStatus());
        assertEquals(CoverageService.DifficultyTargetStatus.SATISFIED,
                result.chapters().get(0).topics().get(0).knowledgeItems().get(0).difficultyTargetStatus());
        assertEquals(CoverageService.DifficultyTargetStatus.NOT_CONFIGURED,
                result.chapters().get(1).topics().get(0).knowledgeItems().get(1).difficultyTargetStatus());
        assertEquals(1, result.legacyQuestionsWithoutKnowledgeItem());
        verify(questions).approvedCoverage(subjectId);
    }

    @Test
    void returnsDeterministicZeroWhenSubjectHasNoKnowledgeItems() {
        when(catalog.findChapters(subjectId)).thenReturn(List.of());
        when(questions.approvedCoverage(subjectId)).thenReturn(List.of());

        var result = service.calculate(subjectId, actor(Role.SUBJECT_ADMIN, "CNTT"));

        assertEquals(0, result.overall().totalKnowledgeItems());
        assertEquals(0, result.overall().coveredKnowledgeItems());
        assertEquals(0.0, result.overall().coveragePercentage());
    }

    @Test
    void managingAndParticipatingSubjectAdminsCanReadSharedSubject() {
        assertEquals(subjectId, service.calculate(subjectId, actor(Role.SUBJECT_ADMIN, "CNTT")).subjectId());
        assertEquals(subjectId, service.calculate(subjectId, actor(Role.SUBJECT_ADMIN, "KT")).subjectId());
    }

    @Test
    void rejectsSystemAdminUserAndOutOfScopeSubjectAdmin() {
        assertThrows(ForbiddenException.class, () -> service.calculate(subjectId, actor(Role.SYSTEM_ADMIN, null)));
        assertThrows(ForbiddenException.class, () -> service.calculate(subjectId, actor(Role.USER, "CNTT")));
        assertThrows(ForbiddenException.class, () -> service.calculate(subjectId, actor(Role.SUBJECT_ADMIN, "XD")));
    }

    private KnowledgeItem item(UUID id, UUID topicId, String code, int ordinal,
                               int targetEasy, int targetMedium, int targetHard) {
        return new KnowledgeItem(id, topicId, code, code, ordinal, targetEasy, targetMedium, targetHard, NOW, NOW);
    }

    private Actor actor(Role role, String facultyId) {
        return new Actor(UUID.randomUUID(), role, facultyId);
    }
}
