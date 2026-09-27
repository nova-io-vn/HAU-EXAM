package com.aiservice.infrastructure.external;
import com.aiservice.application.port.out.SubjectAuthorizationPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.UUID;
@Component
public class SubjectAuthorizationRestClient implements SubjectAuthorizationPort {
    private final RestClient client; private final String baseUrl, token;
    public SubjectAuthorizationRestClient(RestClient.Builder builder, @Value("${question.service.url:http://question-service}") String baseUrl, @Value("${ai.internal.result-token}") String token) { this.client = builder.build(); this.baseUrl = baseUrl; this.token = token; }
    public boolean assigned(UUID subjectId, UUID userId) { try { return Boolean.TRUE.equals(client.get().uri(baseUrl + "/api/v1/internal/subjects/{subjectId}/assignments/{userId}", subjectId, userId).header("X-Internal-Service-Token", token).retrieve().body(Boolean.class)); } catch (org.springframework.web.client.RestClientResponseException e) { return false; } }
}
