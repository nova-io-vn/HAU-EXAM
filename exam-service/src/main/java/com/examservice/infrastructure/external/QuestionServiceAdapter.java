package com.examservice.infrastructure.external;

import com.examservice.application.port.out.QuestionCatalogPort;
import com.examservice.domain.exception.DomainException;
import com.examservice.domain.model.Difficulty;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class QuestionServiceAdapter implements QuestionCatalogPort {
    private static final int PAGE_SIZE = 100;
    private final RestClient client;
    private final ObjectMapper mapper;
    private final String baseUrl;

    public QuestionServiceAdapter(@LoadBalanced RestClient.Builder builder, ObjectMapper mapper,
                                  @Value("${exam.question-service-url}") String baseUrl) {
        this.client = builder.build(); this.mapper = mapper; this.baseUrl = baseUrl;
    }

    public List<QuestionCandidate> approvedQuestions(String facultyId, UUID subjectId, UUID chapterId,
                                                     UUID topicId, UUID knowledgeItemId, Difficulty difficulty,
                                                     String token) {
        try {
            List<QuestionCandidate> result = new ArrayList<>();
            int page = 0;
            int totalPages;
            do {
                int requestedPage = page;
                String raw = client.get().uri(builder -> {
                    builder.path(baseUrl + "/api/v1/questions/approved")
                            .queryParam("subjectId", subjectId)
                            .queryParam("chapterId", chapterId)
                            .queryParamIfPresent("topicId", Optional.ofNullable(topicId))
                            .queryParam("difficulty", difficulty)
                            .queryParam("page", requestedPage)
                            .queryParam("size", PAGE_SIZE)
                            .queryParam("sort", "createdAt,asc");
                    return builder.build();
                }).headers(headers -> headers.setBearerAuth(token)).retrieve().body(String.class);
                JsonNode data = mapper.readTree(raw).at("/data");
                JsonNode items = data.get("items");
                if (items == null || !items.isArray()) throw new DomainException("Invalid Question Service response");
                for (JsonNode question : items) {
                    QuestionCandidate candidate = candidate(question);
                    if (matches(candidate, facultyId, subjectId, chapterId, topicId, knowledgeItemId, difficulty))
                        result.add(candidate);
                }
                totalPages = data.path("totalPages").asInt(1);
                page++;
            } while (page < totalPages);
            return result;
        } catch (RestClientException exception) {
            throw new DomainException("Question Service unavailable");
        } catch (DomainException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new DomainException("Invalid Question Service response");
        }
    }

    public QuestionDetails question(UUID id, String token) {
        try {
            String raw = client.get().uri(baseUrl + "/api/v1/questions/{id}", id)
                    .headers(headers -> headers.setBearerAuth(token)).retrieve().body(String.class);
            JsonNode question = mapper.readTree(raw).at("/data");
            if (question.isMissingNode() || question.get("content") == null)
                throw new DomainException("Invalid Question Service response");
            List<Option> options = new ArrayList<>();
            JsonNode values = question.get("options");
            if (values != null && values.isArray()) {
                for (JsonNode option : values) {
                    options.add(new Option(UUID.fromString(option.get("id").asText()), option.path("label").asText(),
                            option.path("content").asText(), option.path("correct").asBoolean(),
                            option.path("sortOrder").asInt()));
                }
            }
            return new QuestionDetails(id, question.path("type").asText(), question.get("content").asText(), options);
        } catch (RestClientException exception) {
            throw new DomainException("Question Service unavailable");
        } catch (DomainException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new DomainException("Invalid Question Service response");
        }
    }

    private QuestionCandidate candidate(JsonNode question) {
        return new QuestionCandidate(UUID.fromString(question.get("id").asText()),
                question.get("facultyId").asText(), UUID.fromString(question.get("subjectId").asText()),
                UUID.fromString(question.get("chapterId").asText()), uuid(question.get("topicId")),
                uuid(question.get("knowledgeItemId")), Difficulty.valueOf(question.get("difficulty").asText()),
                question.get("status").asText());
    }

    private boolean matches(QuestionCandidate question, String facultyId, UUID subjectId, UUID chapterId,
                            UUID topicId, UUID knowledgeItemId, Difficulty difficulty) {
        return "APPROVED".equals(question.status()) && facultyId.equals(question.facultyId())
                && subjectId.equals(question.subjectId()) && chapterId.equals(question.chapterId())
                && java.util.Objects.equals(topicId, question.topicId())
                && (knowledgeItemId == null || java.util.Objects.equals(knowledgeItemId, question.knowledgeItemId()))
                && difficulty == question.difficulty();
    }

    private static UUID uuid(JsonNode node) {
        return node == null || node.isNull() ? null : UUID.fromString(node.asText());
    }
}
