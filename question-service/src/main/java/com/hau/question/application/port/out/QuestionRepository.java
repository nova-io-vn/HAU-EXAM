package com.hau.question.application.port.out;
import com.hau.question.application.model.*; import com.hau.question.domain.model.Question;
import java.util.*;
public interface QuestionRepository { Question save(Question q); Optional<Question> findById(UUID id); PageResult<Question> search(QuestionCriteria c); void delete(UUID id); boolean existsByAiSourceId(String id); }
