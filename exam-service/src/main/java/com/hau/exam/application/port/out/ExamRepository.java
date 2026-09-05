package com.hau.exam.application.port.out;import com.hau.exam.domain.model.Exam;import java.util.*;public interface ExamRepository{Exam save(Exam e);Optional<Exam>findById(UUID id);}
