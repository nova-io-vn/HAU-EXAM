package com.examservice.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "exam_question_references")
public class ExamQuestionEntity {
    @Id public UUID id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "exam_version_id", nullable = false) public ExamVersionEntity version;
    @Column(name = "question_id", nullable = false) public UUID questionId;
    @Column(nullable = false) public int position;
    @Column(name = "matrix_rule_id", nullable = false) public UUID matrixRuleId;

    public UUID questionId() { return questionId; }
    public int position() { return position; }
    public UUID matrixRuleId() { return matrixRuleId; }
}
