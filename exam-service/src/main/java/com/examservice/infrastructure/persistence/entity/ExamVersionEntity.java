package com.examservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "exam_versions")
public class ExamVersionEntity {
    @Id public UUID id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "exam_id", nullable = false) public ExamEntity exam;
    @Column(name = "version_number", nullable = false) public int versionNumber;
    @Column(name = "created_by", nullable = false) public UUID createdBy;
    @Column(name = "generation_seed", nullable = false) public long generationSeed;
    @Column(name = "question_count", nullable = false) public int questionCount;
    @Column(name = "created_at", nullable = false) public Instant createdAt;
    @OneToMany(mappedBy = "version", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("position") public List<ExamQuestionEntity> questions = new ArrayList<>();
}
