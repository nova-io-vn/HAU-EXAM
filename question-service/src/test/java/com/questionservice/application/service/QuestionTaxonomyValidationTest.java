package com.questionservice.application.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.questionservice.application.model.QuestionInput;
import com.questionservice.application.port.out.CatalogRepository;
import com.questionservice.application.port.out.QuestionEventPublisher;
import com.questionservice.application.port.out.QuestionRepository;
import com.questionservice.domain.model.Actor;
import com.questionservice.domain.model.Chapter;
import com.questionservice.domain.model.Difficulty;
import com.questionservice.domain.model.KnowledgeItem;
import com.questionservice.domain.model.QuestionOption;
import com.questionservice.domain.model.QuestionType;
import com.questionservice.domain.model.Role;
import com.questionservice.domain.model.Subject;
import com.questionservice.domain.model.Topic;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class QuestionTaxonomyValidationTest {
    @Test
    void rejectsKnowledgeItemOutsideSelectedTopic() {
        QuestionRepository questions = mock(QuestionRepository.class);
        QuestionEventPublisher events = mock(QuestionEventPublisher.class);
        CatalogRepository catalog = mock(CatalogRepository.class);
        QuestionService service = new QuestionService(questions, events,
                Clock.fixed(Instant.parse("2026-09-29T00:00:00Z"), ZoneOffset.UTC), catalog);
        UUID subjectId = UUID.randomUUID();
        UUID chapterId = UUID.randomUUID();
        UUID topicId = UUID.randomUUID();
        UUID otherTopicId = UUID.randomUUID();
        UUID knowledgeItemId = UUID.randomUUID();
        Instant now = Instant.parse("2026-09-29T00:00:00Z");
        when(catalog.findSubject(subjectId)).thenReturn(Optional.of(new Subject(subjectId, "CNTT", Set.of("CNTT"), "S1", "Subject", now, now)));
        when(catalog.findChapter(chapterId)).thenReturn(Optional.of(new Chapter(chapterId, subjectId, "C1", "Chapter", 1, now, now)));
        when(catalog.findTopic(topicId)).thenReturn(Optional.of(new Topic(topicId, chapterId, "T1", "Topic", now, now)));
        when(catalog.findKnowledgeItem(knowledgeItemId)).thenReturn(Optional.of(
                new KnowledgeItem(knowledgeItemId, otherTopicId, "K1", "Knowledge", 1, 0, 0, 0, now, now)));
        QuestionInput input = new QuestionInput("CNTT", subjectId, chapterId, topicId, knowledgeItemId, null,
                "Question", null, null, QuestionType.SINGLE_CHOICE, Difficulty.EASY,
                List.of(new QuestionOption(null, "A", "A", null, null, true, 0),
                        new QuestionOption(null, "B", "B", null, null, false, 1)));

        assertThrows(IllegalArgumentException.class,
                () -> service.create(new Actor(UUID.randomUUID(), Role.USER, "CNTT"), input));
        verifyNoInteractions(questions);
    }
}
