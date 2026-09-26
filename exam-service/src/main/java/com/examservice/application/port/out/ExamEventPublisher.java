package com.examservice.application.port.out;
import com.examservice.domain.model.Exam;
import java.util.UUID;
public interface ExamEventPublisher { void generated(Exam exam, UUID requestedBy); }
