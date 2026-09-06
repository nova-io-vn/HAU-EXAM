package com.notificationservice.infrastructure.audience;

import com.notificationservice.application.dto.Recipient;
import com.notificationservice.application.port.out.AudienceResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Component
public class UserServiceAudienceResolver implements AudienceResolver {
    private final RestClient client;
    private final String baseUrl;
    private final String serviceToken;

    public UserServiceAudienceResolver(RestClient.Builder builder,
                                       @Value("${user.service.url}") String baseUrl,
                                       @Value("${user.internal.service-token}") String serviceToken) {
        if (baseUrl == null || baseUrl.isBlank()) throw new IllegalStateException("USER_SERVICE_URL is required");
        if (serviceToken == null || serviceToken.isBlank()) throw new IllegalStateException("INTERNAL_SERVICE_TOKEN is required");
        this.client = builder.build();
        this.baseUrl = baseUrl;
        this.serviceToken = serviceToken;
    }

    @Override
    public List<Recipient> resolve(String role, String faculty) {
        var uriBuilder = UriComponentsBuilder.fromUriString(baseUrl + "/api/v1/internal/users/audience");
        if (role != null && !role.isBlank()) uriBuilder.queryParam("role", role);
        if (faculty != null && !faculty.isBlank()) uriBuilder.queryParam("facultyId", faculty);
        AudienceResponse response = client.get()
                .uri(uriBuilder.build().toUri())
                .header("X-Internal-Service-Token", serviceToken)
                .retrieve()
                .body(AudienceResponse.class);
        if (response == null || !response.success() || response.data() == null) {
            throw new IllegalStateException("User Service returned an invalid scheduled audience response");
        }
        return response.data().stream().map(item -> new Recipient(item.userId(), item.email())).toList();
    }

    record AudienceResponse(boolean success, String code, String message, List<AudienceMember> data) { }
    record AudienceMember(java.util.UUID userId, String email) { }
}
