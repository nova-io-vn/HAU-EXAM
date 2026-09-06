package com.questionservice.application.service;
import com.questionservice.application.port.out.*;
import com.questionservice.domain.model.*;
import com.questionservice.infrastructure.rabbitmq.AiGenerationPayload;
import java.time.Clock;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
class AiQuestionImportServiceTest {
 @Test void fetchesReferenceAndCreatesDraftQuestion(){var q=mock(QuestionRepository.class);var e=mock(ProcessedEventRepository.class);var client=mock(AiResultClient.class);UUID event=UUID.randomUUID(),job=UUID.randomUUID(),user=UUID.randomUUID();when(e.exists(event)).thenReturn(false);when(q.existsByAiSourceId(anyString())).thenReturn(false);when(client.fetch(job,"db:ai-results:"+job)).thenReturn(result(job));var s=new AiQuestionImportService(q,e,client,Clock.systemUTC());var p=new AiGenerationPayload(job,user,"CNTT",UUID.randomUUID(),UUID.randomUUID(),null,"db:ai-results:"+job);assertEquals(1,s.importCompleted(p,event));verify(q).save(argThat(x->x.source()==QuestionSource.AI&&x.status()==QuestionStatus.DRAFT));verify(e).record(event,"AI_GENERATION_COMPLETED");}
 @Test void duplicateEventDoesNotFetchOrDuplicate(){var q=mock(QuestionRepository.class);var e=mock(ProcessedEventRepository.class);var client=mock(AiResultClient.class);UUID event=UUID.randomUUID();when(e.exists(event)).thenReturn(true);var s=new AiQuestionImportService(q,e,client,Clock.systemUTC());assertEquals(0,s.importCompleted(new AiGenerationPayload(UUID.randomUUID(),UUID.randomUUID(),"CNTT",UUID.randomUUID(),UUID.randomUUID(),null,"ref"),event));verifyNoInteractions(client,q);}
 @Test void missingRequiredContextFailsBeforeFetch(){var q=mock(QuestionRepository.class);var e=mock(ProcessedEventRepository.class);var client=mock(AiResultClient.class);UUID event=UUID.randomUUID();when(e.exists(event)).thenReturn(false);var s=new AiQuestionImportService(q,e,client,Clock.systemUTC());assertThrows(IllegalArgumentException.class,()->s.importCompleted(new AiGenerationPayload(UUID.randomUUID(),UUID.randomUUID(),null,UUID.randomUUID(),UUID.randomUUID(),null,"ref"),event));verifyNoInteractions(client);}
 @Test void temporaryResultFailureIsPropagated(){var q=mock(QuestionRepository.class);var e=mock(ProcessedEventRepository.class);var client=mock(AiResultClient.class);UUID event=UUID.randomUUID(),job=UUID.randomUUID();when(e.exists(event)).thenReturn(false);when(client.fetch(any(),anyString())).thenThrow(new IllegalStateException("temporary"));var s=new AiQuestionImportService(q,e,client,Clock.systemUTC());assertThrows(IllegalStateException.class,()->s.importCompleted(new AiGenerationPayload(job,UUID.randomUUID(),"CNTT",UUID.randomUUID(),UUID.randomUUID(),null,"ref"),event));verify(e,never()).record(any(),anyString());}
 private AiResultClient.GeneratedResult result(UUID job){return new AiResultClient.GeneratedResult(List.of(new AiResultClient.GeneratedQuestion("0","Q",null,null,QuestionType.SINGLE_CHOICE,Difficulty.EASY,null,List.of(new AiResultClient.GeneratedOption("A","a",null,null,true,0),new AiResultClient.GeneratedOption("B","b",null,null,false,1)))));}
}
