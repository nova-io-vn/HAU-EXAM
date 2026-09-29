package com.examservice.infrastructure.persistence.entity;

import com.examservice.domain.model.ExamStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "exams")
public class ExamEntity {
    @Id public UUID id;
    @Column(nullable = false) public String name;
    @Column(name = "exam_code", nullable = false) public String examCode;
    @Column(name = "duration_minutes", nullable = false) public int durationMinutes;
    @Enumerated(EnumType.STRING) @Column(nullable = false) public ExamStatus status;
    @Column(name = "faculty_id", nullable = false) public String facultyId;
    @Column(name = "subject_id", nullable = false) public UUID subjectId;
    @Column(name = "matrix_id", nullable = false) public UUID matrixId;
    @Column(name = "template_id") public UUID templateId;
    @Column(name = "created_by", nullable = false) public UUID createdBy;
    @Column(name = "version_start_code", nullable = false) public int versionStartCode;
    @Column(name = "version_end_code", nullable = false) public int versionEndCode;
    @Column(name = "shuffle_questions", nullable = false) public boolean shuffleQuestions;
    @Column(name = "shuffle_answers", nullable = false) public boolean shuffleAnswers;
    @Column(name = "allow_question_replacement", nullable = false) public boolean allowQuestionReplacement;
    @Column(name = "reuse_warning", nullable = false) public boolean reuseWarning;
    @Column(name = "created_at", nullable = false) public Instant createdAt;
    @Column(name = "updated_at", nullable = false) public Instant updatedAt;
    @Version public long version;
    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("versionNumber")
    public List<ExamVersionEntity> versions = new ArrayList<>();
}
