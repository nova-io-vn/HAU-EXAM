package com.aiservice.infrastructure.external;

import com.aiservice.application.port.out.UserDirectoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;
import java.util.UUID;

@Component
public class UserDirectoryRestClient implements UserDirectoryPort {
    private final RestClient client;
    private final ObjectMapper mapper;
    private final String baseUrl;
    private final String token;

    public UserDirectoryRestClient(RestClient.Builder builder, ObjectMapper mapper,
                                  @Value("${user.service.url:http://user-service}") String baseUrl,
                                  @Value("${ai.internal.service-token:${INTERNAL_SERVICE_TOKEN:dev-internal-token}}") String token) {
        this.client = builder.build(); this.mapper = mapper; this.baseUrl = baseUrl; this.token = token;
    }

    @Override
    public String displayName(UUID userId) {
        try {
            String body = client.get().uri(baseUrl + "/api/v1/internal/users/{id}/contact", userId)
                    .header("X-Internal-Service-Token", token).retrieve().body(String.class);
            var data = mapper.readTree(body).get("data");
            if (data == null) return null;
            String name = data.path("displayName").asString(null);
            return name == null ? data.path("fullName").asString(null) : name;
        } catch (Exception ignored) { return null; }
    }
}
