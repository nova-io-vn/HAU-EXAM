package com.hau.question.infrastructure.persistence.repository;
import com.hau.question.infrastructure.persistence.entity.ProcessedEventEntity; import java.util.UUID; import org.springframework.data.jpa.repository.JpaRepository;
public interface ProcessedEventJpaRepository extends JpaRepository<ProcessedEventEntity,UUID>{}
