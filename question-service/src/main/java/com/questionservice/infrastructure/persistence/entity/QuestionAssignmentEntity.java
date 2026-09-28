package com.questionservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity @Table(name = "question_assignments")
public class QuestionAssignmentEntity {
    @Id public UUID id;
    @Column(name="faculty_id",nullable=false) public String facultyId;
    @Column(name="subject_id",nullable=false) public UUID subjectId;
    @Column(name="chapter_id") public UUID chapterId;
    @Column(name="topic_id") public UUID topicId;
    @Column(name="knowledge_item_id") public UUID knowledgeItemId;
    @Column(name="lecturer_id",nullable=false) public UUID lecturerId;
    @Column(name="assigned_by",nullable=false) public UUID assignedBy;
    @Column(name="required_question_count",nullable=false) public int requiredQuestionCount;
    @Column(name="required_easy",nullable=false) public int requiredEasy;
    @Column(name="required_medium",nullable=false) public int requiredMedium;
    @Column(name="required_hard",nullable=false) public int requiredHard;
    @Column(nullable=false) public LocalDate deadline;
    @Column(length=1000) public String note;
    @Column(name="created_at",nullable=false) public Instant createdAt;
    @Column(name="updated_at",nullable=false) public Instant updatedAt;
    @Version public long version;
}
