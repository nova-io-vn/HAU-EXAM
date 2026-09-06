package com.authservice.infrastructure.persistence.adapter;

import com.authservice.application.port.out.ProcessedAuthEventStore;
import com.authservice.infrastructure.persistence.entity.ProcessedAuthEventEntity;
import com.authservice.infrastructure.persistence.repository.JpaProcessedAuthEventRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public class ProcessedAuthEventPersistenceAdapter implements ProcessedAuthEventStore {
    private final JpaProcessedAuthEventRepository repository;

    public ProcessedAuthEventPersistenceAdapter(JpaProcessedAuthEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean exists(UUID eventId) {
        return repository.existsById(eventId);
    }

    @Override
    public void record(UUID eventId, String eventType, Instant processedAt) {
        repository.save(new ProcessedAuthEventEntity(eventId, eventType, processedAt));
    }
}
