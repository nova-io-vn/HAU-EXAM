package com.aiservice.infrastructure.external;

import com.aiservice.application.port.out.AiProvider;
import com.aiservice.domain.exception.ProviderException;

import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import tools.jackson.databind.*;
import java.util.regex.Matcher;

@Component
public class GeminiAdapter implements AiProvider {
    private final RestClient client;
    private final ObjectMapper mapper;
    private final String url, key;

    public GeminiAdapter(RestClient.Builder b, ObjectMapper m, @Value("${ai.provider.url}") String u, @Value("${ai.provider.api-key}") String k) {
        client = b.build();
        mapper = m;
        url = u;
        key = k;
    }

    public String generateQuestions(String source, String request) {
        return generateQuestions(source, request, key, null);
    }

    public String generateQuestions(String source, String request, String runtimeKey, String model) {
        String language = request.contains("\"language\":\"EN\"") ? "English" : "Vietnamese";
        return invoke("Return only valid JSON questions with question, options(label/content), correctAnswer, difficulty, topicId, explanation, language, requiresImage and optional imagePrompt/imageUrl. Generate the question, answer options and explanation entirely in " + language + ". Do not translate only the labels.\nSOURCE:\n" + source + "\nREQUEST:\n" + request, runtimeKey, model);
    }

    public String analyze(String source, String request) {
        return analyze(source, request, key, null);
    }

    public String analyze(String source, String request, String runtimeKey, String model) {
        return invoke("Return only a valid JSON analysis object.\nSOURCE:\n" + source + "\nREQUEST:\n" + request, runtimeKey, model);
    }

    public String chat(String source, String request) {
        return chat(source, request, key, null);
    }

    public String chat(String source, String request, String runtimeKey, String model) {
        return invoke("Return only a valid JSON object with an answer field. Do not use knowledge outside the supplied context.\nCONTEXT:\n" + source + "\nREQUEST:\n" + request, runtimeKey, model);
    }

    public String systemHelp(String roleKnowledge, String request) {
        return systemHelp(roleKnowledge, request, key, null);
    }

    public String systemHelp(String roleKnowledge, String request, String runtimeKey, String model) {
        return invoke("You are the HAU QM system help assistant. Answer in Vietnamese unless the user writes in English. "
                + "Only explain workflows present in ROLE_KNOWLEDGE. Give concise numbered steps. Never invent features, permissions, URLs, or route keys. "
                + "If the question is outside HAU QM usage, briefly say this assistant only supports HAU QM. "
                + "Return only JSON: {\"answer\":\"...\",\"actions\":[{\"type\":\"NAVIGATE\",\"label\":\"...\",\"routeKey\":\"...\"}]}. "
                + "Actions are optional and routeKey must occur verbatim in ROLE_KNOWLEDGE.\nROLE_KNOWLEDGE:\n" + roleKnowledge + "\nREQUEST:\n" + request, runtimeKey, model);
    }

    private String invoke(String prompt, String runtimeKey, String model) {
        if (runtimeKey == null || runtimeKey.isBlank()) throw new ProviderException("AI provider credential is not configured", false, null);
        try {
            Map<String, Object> body = Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
            String raw = client.post().uri(configuredUrl(model) + runtimeKey).body(body).retrieve().body(String.class);
            JsonNode n = mapper.readTree(raw);
            JsonNode text = n.at("/candidates/0/content/parts/0/text");
            if (!text.isTextual()) throw new ProviderException("Provider returned no text", false, null);
            return stripFence(text.asText());
        } catch (HttpClientErrorException e) {
            throw new ProviderException("Provider rejected request", e.getStatusCode().is5xxServerError(), e);
        } catch (ResourceAccessException e) {
            throw new ProviderException("Provider timeout or unavailable", true, e);
        } catch (ProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new ProviderException("Malformed provider response", false, e);
        }
    }

    private String configuredUrl(String model) {
        if (model == null || model.isBlank()) return url;
        return url.replaceFirst("(?<=/models/)[^:]+", Matcher.quoteReplacement(model.trim()));
    }

    private String stripFence(String s) {
        String t = s.trim();
        if (t.startsWith("```")) {
            int first = t.indexOf('\n');
            int last = t.lastIndexOf("```");
            if (first >= 0 && last > first) t = t.substring(first + 1, last).trim();
        }
        return t;
    }
}
