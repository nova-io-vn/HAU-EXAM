package com.questionservice.application.service;
import com.questionservice.application.port.out.*;
import com.questionservice.domain.model.*;
import com.questionservice.infrastructure.rabbitmq.AiGenerationPayload;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service public class AiQuestionImportService {
 private final QuestionRepository questions; private final ProcessedEventRepository events; private final AiResultClient results; private final Clock clock;
 public AiQuestionImportService(QuestionRepository q,ProcessedEventRepository e,AiResultClient r,Clock c){questions=q;events=e;results=r;clock=c;}
 @Transactional public int importCompleted(AiGenerationPayload p,UUID eventId){if(events.exists(eventId))return 0;requireContext(p);var generated=results.fetch(p.jobId(),p.resultReference());if(generated.questions()==null||generated.questions().isEmpty())throw new IllegalArgumentException("AI result contains no questions");int count=0;for(var item:generated.questions()){String source=p.jobId()+":"+item.sourceId();if(questions.existsByAiSourceId(source))continue;var opts=item.options().stream().map(o->new QuestionOption(UUID.randomUUID(),o.label(),o.content(),o.imageUrl(),o.storageKey(),o.correct(),o.sortOrder())).toList();UUID topic=item.topicId()==null?p.topicId():item.topicId();questions.save(Question.create(UUID.randomUUID(),p.facultyId(),p.subjectId(),p.chapterId(),topic,item.content(),item.imageUrl(),item.storageKey(),item.type(),item.difficulty(),QuestionSource.AI,source,p.requestedBy(),opts,Instant.now(clock)));count++;}events.record(eventId,"AI_GENERATION_COMPLETED");return count;}
 private void requireContext(AiGenerationPayload p){if(p==null||p.jobId()==null||p.requestedBy()==null||p.resultReference()==null||p.resultReference().isBlank()||p.facultyId()==null||p.facultyId().isBlank()||p.subjectId()==null||p.chapterId()==null)throw new IllegalArgumentException("AI generation event context is incomplete");}
}
