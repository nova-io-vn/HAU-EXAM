package com.aiservice.application.port.out;

public interface AiProvider {
    String generateQuestions(String sourceText, String requestJson);
    String analyze(String sourceText, String requestJson);
    String chat(String sourceText, String requestJson);
}
