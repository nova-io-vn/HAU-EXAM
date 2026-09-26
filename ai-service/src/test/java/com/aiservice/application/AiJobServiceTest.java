package com.aiservice.application;

import com.aiservice.application.port.out.AiEventPublisher;
import com.aiservice.application.port.out.AiJobRepository;
import com.aiservice.application.port.out.DocumentRepository;
import com.aiservice.application.service.AiJobService;
import com.aiservice.domain.model.JobType;
import java.time.Clock;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class AiJobServiceTest {
    private final AiJobRepository jobs = mock(AiJobRepository.class);
    private final DocumentRepository documents = mock(DocumentRepository.class);
    private final AiEventPublisher events = mock(AiEventPublisher.class);
    private final AiJobService service = new AiJobService(jobs, documents, events, Clock.systemUTC());

    @Test
    void questionGenerationRejectsMissingCatalogContextBeforeCreatingJob() {
        assertThrows(IllegalArgumentException.class, () -> service.create(
                UUID.randomUUID(), UUID.randomUUID(), JobType.QUESTION_GENERATION, "{}", UUID.randomUUID(),
                "CNTT", null, null, null));

        verifyNoInteractions(jobs, documents, events);
    }
}
