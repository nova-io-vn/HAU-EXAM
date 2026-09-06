package com.aiservice.presentation.response;

import com.aiservice.domain.model.*;

import java.time.Instant;
import java.util.UUID;

public final class AiResponses {
    private AiResponses() {
    }

    public record DocumentView(UUID id, String originalName, String contentType, long size, String storageKey,
                               String checksum, Instant createdAt) {
        public static DocumentView from(DocumentMetadata d) {
            return new DocumentView(d.id(), d.originalName(), d.contentType(), d.size(), d.storageKey(), d.checksum(), d.createdAt());
        }
    }

    public record JobView(UUID jobId, JobType type, JobStatus status, String resultReference, String errorCode,
                          String errorMessage, Instant createdAt, Instant startedAt, Instant completedAt) {
        public static JobView from(AiJob j) {
            return new JobView(j.id(), j.type(), j.status(), j.resultReference(), j.errorCode(), j.errorMessage(), j.createdAt(), j.startedAt(), j.completedAt());
        }
    }
}
