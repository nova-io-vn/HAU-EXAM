package com.hau.ai.application;

import com.hau.ai.application.port.out.*;
import com.hau.ai.application.service.*;
import com.hau.ai.domain.exception.*;
import com.hau.ai.domain.model.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AiWorkspaceServiceTest {
    private final DocumentRepository documents = mock(DocumentRepository.class);
    private final AiJobRepository jobs = mock(AiJobRepository.class);
    private final AiResultRepository results = mock(AiResultRepository.class);
    private final AiJobService jobService = new AiJobService(jobs, documents, mock(AiEventPublisher.class), Clock.systemUTC());
    private final AiWorkspaceService workspace = new AiWorkspaceService(documents, jobs, results, jobService);

    @Test void anotherOwnerCannotReadResult() {
        var job = AiJob.pending(UUID.randomUUID(), UUID.randomUUID(), null, JobType.CHAT, "{}", Instant.now());
        when(jobs.findById(job.id())).thenReturn(Optional.of(job));
        assertThrows(NotFoundException.class, () -> workspace.result(job.id(), UUID.randomUUID()));
        verifyNoInteractions(results);
    }

    @Test void pendingJobDoesNotExposeResult() {
        var owner = UUID.randomUUID();
        var job = AiJob.pending(UUID.randomUUID(), owner, null, JobType.CHAT, "{}", Instant.now());
        when(jobs.findById(job.id())).thenReturn(Optional.of(job));
        assertThrows(InvalidJobTransitionException.class, () -> workspace.result(job.id(), owner));
        verifyNoInteractions(results);
    }

    @Test void completedOwnerCanReadStructuredResult() {
        var owner = UUID.randomUUID();
        var job = AiJob.pending(UUID.randomUUID(), owner, null, JobType.CHAT, "{}", Instant.now());
        job.start(Instant.now());
        job.complete("db:ai-results:" + job.id(), Instant.now());
        when(jobs.findById(job.id())).thenReturn(Optional.of(job));
        when(results.findByJobId(job.id())).thenReturn(Optional.of("{\"answer\":\"hello\"}"));
        assertEquals("{\"answer\":\"hello\"}", workspace.result(job.id(), owner));
    }

    @Test void listsDelegateOwnerAndRejectUnboundedPages() {
        var owner = UUID.randomUUID();
        workspace.documents(owner, 2, 20);
        workspace.jobs(owner, 0, 20);
        verify(documents).findByOwner(owner, 2, 20);
        verify(jobs).findByOwner(owner, 0, 20);
        assertThrows(IllegalArgumentException.class, () -> workspace.documents(owner, -1, 20));
        assertThrows(IllegalArgumentException.class, () -> workspace.jobs(owner, 0, 101));
    }
}
