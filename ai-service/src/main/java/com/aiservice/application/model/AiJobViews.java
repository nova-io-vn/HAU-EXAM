package com.aiservice.application.model;

import com.aiservice.application.port.out.CatalogDirectoryPort.CatalogContext;
import com.aiservice.domain.model.*;
import java.time.Instant;
import java.util.*;

public final class AiJobViews {
    private AiJobViews() { }
    public record Creator(UUID userId, String lecturerCode, String fullName, String facultyId,
                          String academicRank, String academicDegree, String avatarUrl) { }
    public record Summary(UUID jobId, String jobCode, JobType type, JobStatus status, int progress,
                          Creator creator, CatalogContext context, Integer generatedCount,
                          Instant createdAt, Instant startedAt, Instant completedAt, Instant updatedAt) { }
    public record DocumentContext(UUID id, String originalFilename, String contentType, long size, String extractedStatus) { }
    public record Detail(Summary summary, String sourceType, DocumentContext document, String description,
                         Map<String,Object> generationConfig, String provider, String model,
                         Integer acceptedCount, Integer rejectedCount, String resultJson,
                         String errorCode, String errorMessage) { }
}
