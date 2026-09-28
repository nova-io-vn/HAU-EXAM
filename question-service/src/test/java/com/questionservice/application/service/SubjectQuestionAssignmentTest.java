package com.questionservice.application.service;

import com.questionservice.application.model.QuestionInput;
import com.questionservice.application.port.out.CatalogRepository;
import com.questionservice.application.port.out.QuestionEventPublisher;
import com.questionservice.application.port.out.QuestionRepository;
import com.questionservice.application.port.out.SubjectAssignmentRepository;
import com.questionservice.domain.exception.ForbiddenException;
import com.questionservice.domain.model.Actor;
import com.questionservice.domain.model.Chapter;
import com.questionservice.domain.model.Difficulty;
import com.questionservice.domain.model.QuestionOption;
import com.questionservice.domain.model.QuestionType;
import com.questionservice.domain.model.Role;
import com.questionservice.domain.model.Subject;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SubjectQuestionAssignmentTest {
    private final QuestionRepository questions = mock(QuestionRepository.class);
    private final QuestionEventPublisher events = mock(QuestionEventPublisher.class);
    private final CatalogRepository catalog = mock(CatalogRepository.class);
    private final SubjectAssignmentRepository assignments = mock(SubjectAssignmentRepository.class);
    private final QuestionService service = new QuestionService(questions, events,
            Clock.fixed(Instant.parse("2026-09-28T00:00:00Z"), ZoneOffset.UTC), catalog, null, assignments);
    private final UUID subjectId = UUID.randomUUID();
    private final UUID chapterId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @Test
    void assignedUserInParticipatingFacultyCanCreateQuestion() {
        arrangeCatalog();
        when(assignments.existsActive(subjectId, userId)).thenReturn(true);
        when(questions.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(new Actor(userId, Role.USER, "KT"), input());

        verify(questions).save(any());
    }

    @Test
    void unassignedUserReceivesSemanticForbiddenCode() {
        arrangeCatalog();
        when(assignments.existsActive(subjectId, userId)).thenReturn(false);

        ForbiddenException error = assertThrows(ForbiddenException.class,
                () -> service.create(new Actor(userId, Role.USER, "KT"), input()));

        assertEquals("SUBJECT_NOT_ASSIGNED", error.code());
    }

    private void arrangeCatalog() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        when(catalog.findSubject(subjectId)).thenReturn(Optional.of(
                new Subject(subjectId, "CNTT", Set.of("CNTT", "KT"), "TRIET", "Triết học", now, now)));
        when(catalog.findChapter(chapterId)).thenReturn(Optional.of(
                new Chapter(chapterId, subjectId, "C1", "Chương 1", 1, now, now)));
    }

    private QuestionInput input() {
        return new QuestionInput("KT", subjectId, chapterId, null, "Nội dung", null, null,
                QuestionType.SINGLE_CHOICE, Difficulty.EASY, List.of(
                new QuestionOption(null, "A", "Đúng", null, null, true, 0),
                new QuestionOption(null, "B", "Sai", null, null, false, 1)));
    }
}
