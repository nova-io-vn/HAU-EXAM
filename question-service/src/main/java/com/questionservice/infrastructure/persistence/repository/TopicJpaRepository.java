package com.questionservice.infrastructure.persistence.repository;
import com.questionservice.infrastructure.persistence.entity.TopicEntity; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface TopicJpaRepository extends JpaRepository<TopicEntity,UUID>{List<TopicEntity> findAllByChapterIdOrderByCode(UUID chapterId);}
