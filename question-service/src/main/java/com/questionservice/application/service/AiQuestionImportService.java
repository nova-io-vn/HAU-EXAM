package com.questionservice.application.service;

import com.questionservice.application.port.out.*;
import com.questionservice.domain.model.*;
import com.questionservice.infrastructure.rabbitmq.AiGenerationPayload;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiQuestionImportService {
    private final QuestionRepository questions;
    private final ProcessedEventRepository events;
    private final AiResultClient results;
    private final Clock clock;
    private final CatalogRepository catalog;

    public AiQuestionImportService(QuestionRepository q, ProcessedEventRepository e, AiResultClient r, Clock c) {
        this(q,e,r,c,null);
    }
    @org.springframework.beans.factory.annotation.Autowired
    public AiQuestionImportService(QuestionRepository q, ProcessedEventRepository e, AiResultClient r, Clock c, CatalogRepository catalog) { questions=q;events=e;results=r;clock=c;this.catalog=catalog; }

    @Transactional
    public int importCompleted(AiGenerationPayload p, UUID eventId) {
        if (events.exists(eventId)) return 0;
        requireContext(p);
        validateTaxonomy(p.subjectId(),p.chapterId(),p.topicId(),p.facultyId());
        var generated = results.fetch(p.jobId(), p.resultReference());
        if (generated.questions() == null || generated.questions().isEmpty())
            throw new IllegalArgumentException("AI result contains no questions");
        int count = 0;
        for (var item : generated.questions()) {
            String source = p.jobId() + ":" + item.sourceId();
            if (questions.existsByAiSourceId(source)) continue;
            var opts = item.options().stream().map(o -> new QuestionOption(UUID.randomUUID(), o.label(), o.content(), o.imageUrl(), o.storageKey(), o.correct(), o.sortOrder())).toList();
            UUID topic = item.topicId() == null ? p.topicId() : item.topicId();
            questions.save(Question.create(UUID.randomUUID(), p.facultyId(), p.subjectId(), p.chapterId(), topic, item.content(), item.imageUrl(), item.storageKey(), item.type(), item.difficulty(), QuestionSource.AI, source, p.requestedBy(), opts, Instant.now(clock)));
            count++;
        }
        events.record(eventId, "AI_GENERATION_COMPLETED");
        return count;
    }

    private void requireContext(AiGenerationPayload p) {
        if (p == null || p.jobId() == null || p.requestedBy() == null || p.resultReference() == null || p.resultReference().isBlank() || p.facultyId() == null || p.facultyId().isBlank() || p.subjectId() == null || p.chapterId() == null)
            throw new IllegalArgumentException("AI generation event context is incomplete");
    }
    private void validateTaxonomy(UUID subjectId,UUID chapterId,UUID topicId,String facultyId){
        if(catalog==null)return;
        var subject=catalog.findSubject(subjectId).orElseThrow(()->new IllegalArgumentException("AI subject not found"));
        if(!facultyId.equals(subject.facultyId()))throw new IllegalArgumentException("AI subject is outside faculty scope");
        var chapter=catalog.findChapter(chapterId).orElseThrow(()->new IllegalArgumentException("AI chapter not found"));
        if(!subjectId.equals(chapter.subjectId()))throw new IllegalArgumentException("AI chapter does not belong to subject");
        if(topicId!=null&&catalog.findTopic(topicId).map(topic->chapterId.equals(topic.chapterId())).orElse(false)==false)throw new IllegalArgumentException("AI topic does not belong to chapter");
    }
}
