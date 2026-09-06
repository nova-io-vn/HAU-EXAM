package com.aiservice.application.service;

import com.aiservice.application.port.out.*;
import com.aiservice.domain.exception.*;
import com.aiservice.domain.model.*;

import java.time.*;
import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiJobService {
    private final AiJobRepository jobs;
    private final DocumentRepository docs;
    private final AiEventPublisher events;
    private final Clock clock;

    public AiJobService(AiJobRepository j, DocumentRepository d, AiEventPublisher e, Clock c) {
        jobs = j;
        docs = d;
        events = e;
        clock = c;
    }

    @Transactional
    public AiJob create(UUID user, UUID document, JobType type, String request, UUID correlation) {
        return create(user, document, type, request, correlation, null, null, null, null);
    }

    @Transactional
    public AiJob create(UUID user, UUID document, JobType type, String request, UUID correlation, String faculty, UUID subject, UUID chapter, UUID topic) {
        if (document != null) {
            var d = docs.findById(document).orElseThrow(() -> new NotFoundException("Document not found"));
            if (!d.ownerId().equals(user)) throw new NotFoundException("Document not found");
        }
        var job = jobs.save(AiJob.pending(UUID.randomUUID(), user, document, faculty, subject, chapter, topic, type, request, Instant.now(clock)));
        events.requested(job, correlation);
        return job;
    }

    @Transactional(readOnly = true)
    public AiJob get(UUID id, UUID user) {
        var j = jobs.findById(id).orElseThrow(() -> new NotFoundException("AI job not found"));
        if (!j.requestedBy().equals(user)) throw new NotFoundException("AI job not found");
        return j;
    }
}
