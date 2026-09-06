package com.questionservice.application.port.out;
import com.questionservice.application.model.*; import com.questionservice.domain.model.Question;
import java.util.*;
public interface QuestionRepository { Question save(Question q); Optional<Question> findById(UUID id); PageResult<Question> search(QuestionCriteria c); void delete(UUID id); boolean existsByAiSourceId(String id); }
