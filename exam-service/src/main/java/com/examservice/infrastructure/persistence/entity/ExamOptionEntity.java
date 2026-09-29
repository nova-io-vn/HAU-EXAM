package com.examservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "exam_version_options")
public class ExamOptionEntity {
    @Id public UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_question_reference_id", nullable = false)
    public ExamQuestionEntity question;
    @Column(name = "option_id", nullable = false) public UUID optionId;
    @Column(name = "display_order", nullable = false) public int displayOrder;
}
