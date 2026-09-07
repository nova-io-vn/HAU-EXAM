package com.notificationservice.infrastructure.audience;

import com.notificationservice.application.dto.UserContact;
import com.notificationservice.application.port.out.UserContactResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.UUID;

@Component
public class UserServiceContactResolver implements UserContactResolver {
    private final RestClient client; private final String baseUrl; private final String serviceToken;
    public UserServiceContactResolver(RestClient.Builder builder, @Value("${user.service.url}") String baseUrl, @Value("${user.internal.service-token}") String serviceToken) {
        if (baseUrl == null || baseUrl.isBlank()) throw new IllegalStateException("USER_SERVICE_URL is required");
        if (serviceToken == null || serviceToken.isBlank()) throw new IllegalStateException("INTERNAL_SERVICE_TOKEN is required");
        this.client = builder.build(); this.baseUrl = baseUrl; this.serviceToken = serviceToken;
    }
    @Override public UserContact resolve(UUID userId) {
        ContactResponse response = client.get().uri(baseUrl + "/api/v1/internal/users/" + userId + "/contact")
                .header("X-Internal-Service-Token", serviceToken).retrieve().body(ContactResponse.class);
        if (response == null || !response.success() || response.data() == null) throw new IllegalStateException("User Service returned an invalid user contact response");
        return response.data();
    }
    record ContactResponse(boolean success, String code, String message, UserContact data) { }
}
