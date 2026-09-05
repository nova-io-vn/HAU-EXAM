package com.hau.question.infrastructure.persistence.repository;
import com.hau.question.infrastructure.persistence.entity.TopicEntity; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface TopicJpaRepository extends JpaRepository<TopicEntity,UUID>{List<TopicEntity> findAllByChapterIdOrderByCode(UUID chapterId);}
