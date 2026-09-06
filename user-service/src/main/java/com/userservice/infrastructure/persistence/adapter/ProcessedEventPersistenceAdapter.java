package com.userservice.infrastructure.persistence.adapter;

import com.userservice.application.port.out.ProcessedEventStore;
import com.userservice.infrastructure.persistence.entity.ProcessedEventEntity;
import com.userservice.infrastructure.persistence.repository.JpaProcessedEventRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public class ProcessedEventPersistenceAdapter implements ProcessedEventStore {
    private final JpaProcessedEventRepository repository;

    public ProcessedEventPersistenceAdapter(JpaProcessedEventRepository repository) {
        this.repository = repository;
    }

    public boolean exists(UUID id) {
        return repository.existsById(id);
    }

    public void record(UUID id, String type, Instant at) {
        repository.save(new ProcessedEventEntity(id, type, at));
    }
}
