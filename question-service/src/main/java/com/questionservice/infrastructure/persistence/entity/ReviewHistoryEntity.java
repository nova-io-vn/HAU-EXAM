package com.questionservice.infrastructure.persistence.entity;
import com.questionservice.domain.model.ReviewAction; import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="question_review_history") public class ReviewHistoryEntity {
 @Id public UUID id; @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="question_id",nullable=false) public QuestionEntity question;
 @Column(name="reviewer_id",nullable=false) public UUID reviewerId; @Enumerated(EnumType.STRING) @Column(nullable=false) public ReviewAction action;
 @Column(columnDefinition="text") public String comment; @Column(name="created_at",nullable=false) public Instant createdAt;
}
