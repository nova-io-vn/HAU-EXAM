package com.aiservice.domain;

import com.aiservice.domain.exception.InvalidJobTransitionException;
import com.aiservice.domain.model.AiJob;
import com.aiservice.domain.model.JobStatus;
import com.aiservice.domain.model.JobType;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiJobOperationsTest {
    @Test
    void pendingJobCanBeCancelledButProcessingJobCannotPretendToBeKilled() {
        var now = Instant.parse("2026-10-03T07:00:00Z");
        var pending = AiJob.pending(UUID.randomUUID(), UUID.randomUUID(), null, JobType.CHAT, "{}", now);
        pending.cancel(now.plusSeconds(5));
        assertEquals(JobStatus.CANCELLED, pending.status());

        var processing = AiJob.pending(UUID.randomUUID(), UUID.randomUUID(), null, JobType.CHAT, "{}", now);
        processing.start(now.plusSeconds(1));
        assertThrows(InvalidJobTransitionException.class, () -> processing.cancel(now.plusSeconds(2)));
    }

    @Test
    void onlyFailedJobCanBeMarkedAsRetried() {
        var now = Instant.parse("2026-10-03T07:00:00Z");
        var failed = AiJob.pending(UUID.randomUUID(), UUID.randomUUID(), null, JobType.CHAT, "{}", now);
        failed.fail("PROVIDER_FAILURE", "safe summary", now.plusSeconds(1));
        failed.markRetried(now.plusSeconds(2));
        assertEquals(JobStatus.RETRIED, failed.status());

        var completed = AiJob.pending(UUID.randomUUID(), UUID.randomUUID(), null, JobType.CHAT, "{}", now);
        completed.start(now.plusSeconds(1));
        completed.complete("db:result", now.plusSeconds(2));
        assertThrows(InvalidJobTransitionException.class, () -> completed.markRetried(now.plusSeconds(3)));
    }
}
