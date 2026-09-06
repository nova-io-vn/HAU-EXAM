package com.aiservice.domain.model; import java.util.List;
public record GeneratedQuestion(String question,List<Option> options,String correctAnswer,String difficulty,String topicId,String explanation){public record Option(String label,String content){}}
