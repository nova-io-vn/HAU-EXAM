package com.questionservice.infrastructure.persistence.entity;
import com.questionservice.domain.model.*; import jakarta.persistence.*; import java.time.Instant; import java.util.*;
@Entity @Table(name="questions") public class QuestionEntity {
 @Id public UUID id; @Column(name="faculty_id",nullable=false) public String facultyId;
 @Column(name="subject_id",nullable=false) public UUID subjectId; @Column(name="chapter_id",nullable=false) public UUID chapterId; @Column(name="topic_id") public UUID topicId;
 @Column(nullable=false,columnDefinition="text") public String content; @Column(name="image_url") public String imageUrl; @Column(name="storage_key") public String storageKey;
 @Enumerated(EnumType.STRING) @Column(nullable=false) public QuestionType type; @Enumerated(EnumType.STRING) @Column(nullable=false) public Difficulty difficulty;
 @Enumerated(EnumType.STRING) @Column(nullable=false) public QuestionStatus status; @Enumerated(EnumType.STRING) @Column(nullable=false) public QuestionSource source;
 @Column(name="ai_source_id",unique=true) public String aiSourceId; @Column(name="created_by",nullable=false) public UUID createdBy;
 @Column(name="created_at",nullable=false) public Instant createdAt; @Column(name="updated_at",nullable=false) public Instant updatedAt;
 @Version public long version;
 @OneToMany(mappedBy="question",cascade=CascadeType.ALL,orphanRemoval=true,fetch=FetchType.EAGER) @OrderBy("sortOrder") public List<QuestionOptionEntity> options=new ArrayList<>();
 @OneToMany(mappedBy="question",cascade=CascadeType.ALL,orphanRemoval=true,fetch=FetchType.EAGER) @OrderBy("createdAt") public List<ReviewHistoryEntity> histories=new ArrayList<>();
}
