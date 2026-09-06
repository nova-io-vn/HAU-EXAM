package com.questionservice.infrastructure.persistence.entity;
import jakarta.persistence.*; import java.util.UUID;
@Entity @Table(name="question_options") public class QuestionOptionEntity {
 @Id public UUID id; @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="question_id",nullable=false) public QuestionEntity question;
 @Column(nullable=false) public String label; @Column(nullable=false,columnDefinition="text") public String content;
 @Column(name="image_url") public String imageUrl; @Column(name="storage_key") public String storageKey;
 @Column(name="is_correct",nullable=false) public boolean correct; @Column(name="sort_order",nullable=false) public int sortOrder;
}
