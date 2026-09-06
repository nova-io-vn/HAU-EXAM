package com.questionservice.infrastructure.persistence.repository;
import com.questionservice.infrastructure.persistence.entity.ChapterEntity; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface ChapterJpaRepository extends JpaRepository<ChapterEntity,UUID>{List<ChapterEntity> findAllBySubjectIdOrderByOrdinal(UUID subjectId);}
