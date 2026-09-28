package com.aiservice.infrastructure.external;

import com.aiservice.application.port.out.UserDirectoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.stream.Collectors;

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
    public Optional<UserProfile> find(UUID userId) {
        return Optional.ofNullable(findAll(Set.of(userId)).get(userId));
    }

    @Override
    public Map<UUID, UserProfile> findAll(Set<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) return Map.of();
        try {
            String ids = userIds.stream().map(UUID::toString).collect(Collectors.joining(","));
            String body = client.get().uri(baseUrl + "/api/v1/internal/users/contacts?ids=" + ids)
                    .header("X-Internal-Service-Token", token).retrieve().body(String.class);
            var data = mapper.readTree(body).get("data");
            if (data == null || !data.isArray()) return Map.of();
            Map<UUID, UserProfile> result = new LinkedHashMap<>();
            data.forEach(node -> {
                UUID id = UUID.fromString(node.path("userId").asText());
                result.put(id, new UserProfile(id, text(node,"lecturerCode"), text(node,"fullName"), text(node,"email"),
                        text(node,"facultyId"), text(node,"role"), text(node,"status"), text(node,"academicRank"),
                        text(node,"academicDegree"), text(node,"avatarUrl")));
            });
            return result;
        } catch (Exception ignored) { return Map.of(); }
    }

    private String text(tools.jackson.databind.JsonNode node, String field) {
        String value = node.path(field).asString(null);
        return value == null || value.isBlank() ? null : value;
    }
}
