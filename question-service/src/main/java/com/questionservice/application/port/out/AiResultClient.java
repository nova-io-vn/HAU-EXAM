package com.questionservice.application.port.out;
import com.questionservice.domain.model.Difficulty;
import com.questionservice.domain.model.QuestionType;
import java.util.List;
import java.util.UUID;
public interface AiResultClient {
    GeneratedResult fetch(UUID jobId, String resultReference);
    record GeneratedResult(List<GeneratedQuestion> questions) {}
    record GeneratedQuestion(String sourceId,String content,String imageUrl,String storageKey,QuestionType type,Difficulty difficulty,UUID topicId,List<GeneratedOption> options) {}
    record GeneratedOption(String label,String content,String imageUrl,String storageKey,boolean correct,int sortOrder) {}
}
