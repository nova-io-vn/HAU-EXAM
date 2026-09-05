package com.hau.question.infrastructure.rabbitmq;
import com.hau.question.domain.model.*; import java.util.*;
public record AiGenerationPayload(String jobId,UUID requestedBy,String facultyId,UUID subjectId,UUID chapterId,UUID topicId,List<AiQuestion> questions){
 public record AiQuestion(String sourceId,String content,String imageUrl,String storageKey,QuestionType type,Difficulty difficulty,List<AiOption> options){}
 public record AiOption(String label,String content,String imageUrl,String storageKey,boolean correct,int sortOrder){}
}
