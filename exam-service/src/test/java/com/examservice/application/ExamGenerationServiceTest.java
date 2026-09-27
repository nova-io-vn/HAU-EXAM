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
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExamGenerationServiceTest {
    ExamRepository exams = mock(ExamRepository.class);
    ExamMatrixRepository matrices = mock(ExamMatrixRepository.class);
    ExamTemplateRepository templates = mock(ExamTemplateRepository.class);
    QuestionCatalogPort questions = mock(QuestionCatalogPort.class);
    ExamGenerationService service = new ExamGenerationService(exams, matrices, templates, questions, Clock.systemUTC());
    UUID subject = UUID.randomUUID(), chapter = UUID.randomUUID();
    ExamMatrix matrix;
    Actor admin = new Actor(UUID.randomUUID(), Role.SUBJECT_ADMIN, "CNTT", "token");

    @BeforeEach void set() {
        var rule = new ExamMatrixRule(UUID.randomUUID(), chapter, null, Difficulty.EASY, 2);
        matrix = new ExamMatrix(UUID.randomUUID(), "M", "CNTT", subject, 2, List.of(rule), admin.userId(), Instant.now(), Instant.now());
        when(matrices.findById(matrix.id())).thenReturn(Optional.of(matrix));
        when(exams.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test void insufficientApprovedQuestionsFailsClearly() {
        when(questions.approvedQuestions(anyString(), any(), any(), isNull(), any(), anyString())).thenReturn(List.of(candidate(UUID.randomUUID(), "APPROVED")));
        assertThrows(InsufficientQuestionsException.class, () -> generate(admin));
    }
    @Test void nonApprovedQuestionsAreNeverSelected() {
        when(questions.approvedQuestions(anyString(), any(), any(), isNull(), any(), anyString())).thenReturn(List.of(candidate(UUID.randomUUID(), "DRAFT"), candidate(UUID.randomUUID(), "APPROVED")));
        assertThrows(InsufficientQuestionsException.class, () -> generate(admin));
    }
    @Test void generatedVersionIsConsistent() {
        when(questions.approvedQuestions(anyString(), any(), any(), isNull(), any(), anyString())).thenReturn(List.of(candidate(UUID.randomUUID(), "APPROVED"), candidate(UUID.randomUUID(), "APPROVED")));
        var exam = generate(admin);
        assertEquals("EXAM-001", exam.examCode());
        assertEquals(60, exam.durationMinutes());
        assertEquals(1, exam.versions().getFirst().versionNumber());
        assertEquals(2, exam.versions().getFirst().questions().size());
    }
    @Test void facultyViolationRejected() {
        assertThrows(ForbiddenException.class, () -> generate(new Actor(UUID.randomUUID(), Role.SUBJECT_ADMIN, "KT", "token")));
    }
    private Exam generate(Actor actor) { return service.generate("E", "exam-001", 60, matrix.id(), null, actor); }
    private QuestionCatalogPort.QuestionCandidate candidate(UUID id, String status) {
        return new QuestionCatalogPort.QuestionCandidate(id, "CNTT", subject, chapter, null, Difficulty.EASY, status);
    }
}
