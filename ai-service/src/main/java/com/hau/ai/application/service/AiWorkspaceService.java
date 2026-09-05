package com.hau.ai.application.service;

import com.hau.ai.application.model.WorkspacePage;
import com.hau.ai.application.port.out.*;
import com.hau.ai.domain.exception.*;
import com.hau.ai.domain.model.*;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AiWorkspaceService {
    private final DocumentRepository documents;
    private final AiJobRepository jobs;
    private final AiResultRepository results;
    private final AiJobService jobService;

    public AiWorkspaceService(DocumentRepository documents, AiJobRepository jobs,
                              AiResultRepository results, AiJobService jobService) {
        this.documents = documents;
        this.jobs = jobs;
        this.results = results;
        this.jobService = jobService;
    }

    public WorkspacePage<DocumentMetadata> documents(UUID owner, int page, int size) {
        validatePage(page, size);
        return documents.findByOwner(owner, page, size);
    }

    public WorkspacePage<AiJob> jobs(UUID owner, int page, int size) {
        validatePage(page, size);
        return jobs.findByOwner(owner, page, size);
    }

    public String result(UUID jobId, UUID owner) {
        var job = jobService.get(jobId, owner);
        if (job.status() != JobStatus.COMPLETED) {
            throw new InvalidJobTransitionException("AI result is not ready");
        }
        return results.findByJobId(jobId).orElseThrow(() -> new NotFoundException("AI result not found"));
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("Invalid pagination");
    }
}
