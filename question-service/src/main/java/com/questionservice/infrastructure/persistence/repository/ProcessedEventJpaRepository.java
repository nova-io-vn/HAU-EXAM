package com.questionservice.infrastructure.persistence.repository;
import com.questionservice.infrastructure.persistence.entity.ProcessedEventEntity; import java.util.UUID; import org.springframework.data.jpa.repository.JpaRepository;
public interface ProcessedEventJpaRepository extends JpaRepository<ProcessedEventEntity,UUID>{}
