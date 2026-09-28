package com.aiservice.application;

import com.aiservice.application.port.out.*;
import com.aiservice.application.service.AiJobQueryService;
import com.aiservice.domain.exception.ForbiddenException;
import com.aiservice.domain.model.*;
import org.junit.jupiter.api.*;
import tools.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AiJobQueryServiceTest {
    AiJobRepository jobs=mock(AiJobRepository.class); AiResultRepository results=mock(AiResultRepository.class);
    DocumentRepository documents=mock(DocumentRepository.class); UserDirectoryPort users=mock(UserDirectoryPort.class);
    CatalogDirectoryPort catalogs=mock(CatalogDirectoryPort.class); AiRuntimeMetadataPort runtime=mock(AiRuntimeMetadataPort.class);
    AiJobQueryService service=new AiJobQueryService(jobs,results,documents,users,catalogs,runtime,new ObjectMapper());
    UUID owner=UUID.randomUUID(),subject=UUID.randomUUID(); AiJob job;
    @BeforeEach void setup(){job=AiJob.pending(UUID.randomUUID(),owner,null,"CNTT",subject,UUID.randomUUID(),null,JobType.QUESTION_GENERATION,"{\"description\":\"Tạo câu hỏi\",\"count\":20,\"apiKey\":\"must-not-leak\"}",Instant.parse("2026-09-28T02:30:00Z"));when(jobs.findById(job.id())).thenReturn(Optional.of(job));when(results.findByJobId(job.id())).thenReturn(Optional.empty());when(users.find(owner)).thenReturn(Optional.empty());when(catalogs.resolve(anyList())).thenReturn(List.of());when(runtime.current()).thenReturn(new AiRuntimeMetadataPort.RuntimeMetadata("GEMINI","gemini-2.5-flash"));}
    @Test void systemAdminCanInspectAnyJobAndSecretsAreNotProjected(){var detail=service.detail(job.id(),UUID.randomUUID(),"SYSTEM_ADMIN",null);assertEquals("AI-20260928-"+job.id().toString().substring(0,6).toUpperCase(),detail.summary().jobCode());assertEquals(20,detail.generationConfig().get("count"));assertFalse(detail.generationConfig().containsKey("apiKey"));}
    @Test void subjectAdminCanInspectOnlyOwnFaculty(){assertDoesNotThrow(()->service.detail(job.id(),UUID.randomUUID(),"SUBJECT_ADMIN","CNTT"));assertThrows(ForbiddenException.class,()->service.detail(job.id(),UUID.randomUUID(),"SUBJECT_ADMIN","KIENTRUC"));}
    @Test void userCanInspectOnlyOwnJob(){assertDoesNotThrow(()->service.detail(job.id(),owner,"USER","CNTT"));assertThrows(ForbiddenException.class,()->service.detail(job.id(),UUID.randomUUID(),"USER","CNTT"));}
}
