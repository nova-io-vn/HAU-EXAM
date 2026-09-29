package com.examservice.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.examservice.application.port.out.*;
import com.examservice.application.service.ExamGenerationService;
import com.examservice.domain.exception.*;
import com.examservice.domain.model.*;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExamGenerationServiceTest {
    private final ExamRepository exams = mock(ExamRepository.class);
    private final ExamMatrixRepository matrices = mock(ExamMatrixRepository.class);
    private final ExamTemplateRepository templates = mock(ExamTemplateRepository.class);
    private final QuestionCatalogPort questions = mock(QuestionCatalogPort.class);
    private final ExamGenerationService service = new ExamGenerationService(exams, matrices, templates, questions,
            (exam, user) -> { }, Clock.fixed(Instant.parse("2026-09-29T00:00:00Z"), ZoneOffset.UTC), 20);
    private final UUID subject = UUID.randomUUID(), chapter = UUID.randomUUID(), topic = UUID.randomUUID();
    private final UUID k1 = UUID.randomUUID(), k2 = UUID.randomUUID();
    private final Actor admin = new Actor(UUID.randomUUID(), Role.SUBJECT_ADMIN, "CNTT", "token");
    private ExamMatrix matrix;

    @BeforeEach
    void setUp() {
        matrix = matrix(2, 1, 1);
        when(matrices.findById(matrix.id())).thenReturn(Optional.of(matrix));
        when(exams.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(questions.question(any(), anyString())).thenAnswer(invocation -> details(invocation.getArgument(0)));
    }

    @Test
    void everyVersionSatisfiesEveryMatrixBucketAndUsesApprovedOnly() {
        stub(k1, Difficulty.EASY, candidates(k1, Difficulty.EASY, 6, "APPROVED"));
        stub(k1, Difficulty.MEDIUM, candidates(k1, Difficulty.MEDIUM, 3, "APPROVED"));
        stub(k2, Difficulty.HARD, candidates(k2, Difficulty.HARD, 3, "APPROVED"));

        Exam exam = generate(101, 103, true, true);

        assertEquals(List.of(101, 102, 103), exam.versions().stream().map(ExamVersion::versionCode).toList());
        for (ExamVersion version : exam.versions()) {
            assertEquals(4, version.questionCount());
            Map<UUID, Long> counts = version.questions().stream().collect(
                    Collectors.groupingBy(ExamQuestionReference::matrixRuleId, Collectors.counting()));
            matrix.rules().forEach(rule -> assertEquals((long) rule.questionCount(), counts.get(rule.id())));
        }
        assertEquals(12, exam.uniqueQuestionCount());
        assertEquals(0, exam.reuseCount());
        assertFalse(exam.reuseWarning());
    }

    @Test
    void insufficientUniquePoolSucceedsAndBalancesUnavoidableReuse() {
        matrix = matrix(2, 0, 0);
        when(matrices.findById(matrix.id())).thenReturn(Optional.of(matrix));
        List<QuestionCatalogPort.QuestionCandidate> pool = candidates(k1, Difficulty.EASY, 4, "APPROVED");
        stub(k1, Difficulty.EASY, pool);

        Exam exam = generate(101, 103, true, false);

        assertEquals(3, exam.versions().size());
        assertTrue(exam.reuseWarning());
        assertEquals(2, exam.reuseCount());
        Map<UUID, Long> usage = exam.versions().stream().flatMap(v -> v.questions().stream())
                .collect(Collectors.groupingBy(ExamQuestionReference::questionId, Collectors.counting()));
        assertTrue(Collections.max(usage.values()) - Collections.min(usage.values()) <= 1);
    }

    @Test
    void insufficientMatrixPoolFailsBeforeAnyVersionIsPersisted() {
        stub(k1, Difficulty.EASY, candidates(k1, Difficulty.EASY, 1, "APPROVED"));
        stub(k1, Difficulty.MEDIUM, candidates(k1, Difficulty.MEDIUM, 1, "APPROVED"));
        stub(k2, Difficulty.HARD, candidates(k2, Difficulty.HARD, 1, "APPROVED"));

        InsufficientQuestionsException error = assertThrows(InsufficientQuestionsException.class,
                () -> generate(101, 103, true, false));

        assertTrue(error.getMessage().contains("required=2"));
        assertTrue(error.getMessage().contains("missing=1"));
        verify(exams, never()).save(any());
    }

    @Test
    void nonApprovedCandidateIsNeverSelected() {
        matrix = matrix(1, 0, 0);
        when(matrices.findById(matrix.id())).thenReturn(Optional.of(matrix));
        stub(k1, Difficulty.EASY, List.of(candidate(UUID.randomUUID(), k1, Difficulty.EASY, "PENDING_REVIEW")));
        assertThrows(InsufficientQuestionsException.class, () -> generate(101, 101, true, false));
    }

    @Test
    void shuffledAnswersPersistStableOptionIdsAndDisplayOrders() {
        matrix = matrix(1, 0, 0);
        when(matrices.findById(matrix.id())).thenReturn(Optional.of(matrix));
        UUID questionId = UUID.randomUUID();
        stub(k1, Difficulty.EASY, List.of(candidate(questionId, k1, Difficulty.EASY, "APPROVED")));

        Exam exam = generate(101, 101, true, true);
        List<ExamOptionReference> mappings = exam.versions().getFirst().questions().getFirst().options();

        assertEquals(4, mappings.size());
        assertEquals(Set.of(1, 2, 3, 4), mappings.stream().map(ExamOptionReference::displayOrder).collect(Collectors.toSet()));
        assertEquals(details(questionId).options().stream().map(QuestionCatalogPort.Option::id).collect(Collectors.toSet()),
                mappings.stream().map(ExamOptionReference::optionId).collect(Collectors.toSet()));
    }

    @Test
    void rejectsInvalidRangeMaximumAndWrongFaculty() {
        assertThrows(IllegalArgumentException.class, () -> generate(105, 101, true, false));
        assertThrows(IllegalArgumentException.class, () -> generate(1, 21, true, false));
        assertThrows(ForbiddenException.class, () -> service.generate(command(101, 101, true, false),
                new Actor(UUID.randomUUID(), Role.SUBJECT_ADMIN, "KT", "token")));
        assertThrows(ForbiddenException.class, () -> service.generate(command(101, 101, true, false),
                new Actor(UUID.randomUUID(), Role.USER, "CNTT", "token")));
        assertThrows(ForbiddenException.class, () -> service.generate(command(101, 101, true, false),
                new Actor(UUID.randomUUID(), Role.SYSTEM_ADMIN, "CNTT", "token")));
    }

    private Exam generate(int start, int end, boolean replacement, boolean answerShuffle) {
        return service.generate(command(start, end, replacement, answerShuffle), admin);
    }

    private ExamGenerationService.GenerateCommand command(int start, int end, boolean replacement, boolean answerShuffle) {
        return new ExamGenerationService.GenerateCommand("Thi cuối kỳ", "exam-001", 60, matrix.id(), null,
                start, end, true, answerShuffle, replacement);
    }

    private ExamMatrix matrix(int easy, int medium, int hard) {
        List<ExamMatrixRule> rules = new ArrayList<>();
        if (easy > 0) rules.add(new ExamMatrixRule(UUID.randomUUID(), chapter, topic, k1, Difficulty.EASY, easy));
        if (medium > 0) rules.add(new ExamMatrixRule(UUID.randomUUID(), chapter, topic, k1, Difficulty.MEDIUM, medium));
        if (hard > 0) rules.add(new ExamMatrixRule(UUID.randomUUID(), chapter, topic, k2, Difficulty.HARD, hard));
        int total = easy + medium + hard;
        return new ExamMatrix(UUID.randomUUID(), "M", "CNTT", subject, total, rules, admin.userId(),
                Instant.now(), Instant.now());
    }

    private void stub(UUID knowledgeItem, Difficulty difficulty, List<QuestionCatalogPort.QuestionCandidate> values) {
        when(questions.approvedQuestions(eq("CNTT"), eq(subject), eq(chapter), eq(topic),
                eq(knowledgeItem), eq(difficulty), eq("token"))).thenReturn(values);
    }

    private List<QuestionCatalogPort.QuestionCandidate> candidates(UUID knowledgeItem, Difficulty difficulty,
                                                                    int count, String status) {
        List<QuestionCatalogPort.QuestionCandidate> values = new ArrayList<>();
        for (int index = 0; index < count; index++) values.add(candidate(UUID.randomUUID(), knowledgeItem, difficulty, status));
        return values;
    }

    private QuestionCatalogPort.QuestionCandidate candidate(UUID id, UUID knowledgeItem,
                                                              Difficulty difficulty, String status) {
        return new QuestionCatalogPort.QuestionCandidate(id, "CNTT", subject, chapter, topic,
                knowledgeItem, difficulty, status);
    }

    private QuestionCatalogPort.QuestionDetails details(UUID questionId) {
        return new QuestionCatalogPort.QuestionDetails(questionId, "SINGLE_CHOICE", "2 + 2 bằng bao nhiêu?", List.of(
                new QuestionCatalogPort.Option(UUID.nameUUIDFromBytes((questionId + "-1").getBytes()), "A", "1", false, 1),
                new QuestionCatalogPort.Option(UUID.nameUUIDFromBytes((questionId + "-2").getBytes()), "B", "4", true, 2),
                new QuestionCatalogPort.Option(UUID.nameUUIDFromBytes((questionId + "-3").getBytes()), "C", "3", false, 3),
                new QuestionCatalogPort.Option(UUID.nameUUIDFromBytes((questionId + "-4").getBytes()), "D", "5", false, 4)));
    }
}
