package com.questionservice.infrastructure.external;
import com.questionservice.application.port.out.UserDirectoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;
import java.util.UUID;
@Component
public class UserDirectoryRestClient implements UserDirectoryPort {
    private final RestClient client; private final ObjectMapper mapper; private final String baseUrl, token;
    public UserDirectoryRestClient(RestClient.Builder b, ObjectMapper m, @Value("${user.service.url:http://user-service}") String u, @Value("${question.internal.service-token:${INTERNAL_SERVICE_TOKEN:dev-internal-token}}") String t) { client=b.build(); mapper=m; baseUrl=u; token=t; }
    public boolean isLecturerInFaculty(UUID id, String faculty) { try { String body=client.get().uri(baseUrl+"/api/v1/internal/users/{id}/contact",id).header("X-Internal-Service-Token",token).retrieve().body(String.class); var data=mapper.readTree(body).get("data"); return data!=null && "USER".equals(data.path("role").asText()) && faculty.equals(data.path("facultyId").asText()); } catch (Exception e) { return false; } }
}
