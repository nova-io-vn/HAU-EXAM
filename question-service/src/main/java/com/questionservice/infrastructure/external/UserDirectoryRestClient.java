package com.questionservice.infrastructure.external;

import com.questionservice.application.exception.UserDirectoryException;
import com.questionservice.application.model.LecturerProfile;
import com.questionservice.application.port.out.UserDirectoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class UserDirectoryRestClient implements UserDirectoryPort {
    private final RestClient client;
    private final ObjectMapper mapper;
    private final String baseUrl;
    private final String token;

    public UserDirectoryRestClient(RestClient.Builder builder, ObjectMapper mapper,
                                   @Value("${user.service.url:http://user-service}") String baseUrl,
                                   @Value("${question.internal.service-token:${INTERNAL_SERVICE_TOKEN:dev-internal-token}}") String token) {
        this.client = builder.build();
        this.mapper = mapper;
        this.baseUrl = baseUrl;
        this.token = token;
    }

    @Override
    public Optional<LecturerProfile> findLecturer(UUID userId) {
        try {
            String body = client.get().uri(baseUrl + "/api/v1/internal/users/{id}/contact", userId)
                    .header("X-Internal-Service-Token", token).retrieve().body(String.class);
            JsonNode data = mapper.readTree(body).get("data");
            return data == null || data.isNull() ? Optional.empty() : Optional.of(profile(data));
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) return Optional.empty();
            throw new UserDirectoryException("User Service rejected lecturer lookup", exception);
        } catch (Exception exception) {
            throw new UserDirectoryException("Could not query User Service", exception);
        }
    }

    @Override
    public List<LecturerProfile> findActiveLecturers(Set<String> facultyIds, String keyword) {
        try {
            UriComponentsBuilder uri = UriComponentsBuilder.fromUriString(baseUrl).path("/api/v1/internal/users/lecturers");
            if (facultyIds != null) facultyIds.forEach(faculty -> uri.queryParam("facultyId", faculty));
            if (keyword != null && !keyword.isBlank()) uri.queryParam("keyword", keyword.trim());
            String body = client.get().uri(uri.build().encode().toUri())
                    .header("X-Internal-Service-Token", token).retrieve().body(String.class);
            JsonNode data = mapper.readTree(body).get("data");
            List<LecturerProfile> result = new ArrayList<>();
            if (data != null && data.isArray()) data.forEach(node -> result.add(profile(node)));
            return result;
        } catch (Exception exception) {
            throw new UserDirectoryException("Could not query User Service lecturer directory", exception);
        }
    }

    private LecturerProfile profile(JsonNode data) {
        return new LecturerProfile(UUID.fromString(data.path("userId").asText()), data.path("lecturerCode").asText(),
                data.path("fullName").asText(), data.path("facultyId").asText(), data.path("role").asText(),
                data.path("status").asText(), data.path("academicRank").asText(), data.path("academicDegree").asText(),
                data.path("avatarUrl").isNull() ? null : data.path("avatarUrl").asText());
    }
}
