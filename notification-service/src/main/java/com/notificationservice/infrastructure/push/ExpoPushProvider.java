package com.notificationservice.infrastructure.push;

import com.notificationservice.application.port.out.PushProvider;
import com.notificationservice.domain.model.DeviceToken;
import com.notificationservice.domain.model.Notification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import java.util.Map;

@Component
public class ExpoPushProvider implements PushProvider {
    private final RestClient client;
    private final boolean enabled;

    public ExpoPushProvider(@Value("${push.expo.enabled}") boolean enabled,
                            @Value("${push.expo.url}") String url) {
        this.client = RestClient.builder().baseUrl(url).build(); this.enabled = enabled;
    }

    @Override
    public PushResult send(DeviceToken token, Notification notification) {
        if (!enabled) return PushResult.DISABLED;
        try {
            String body = client.post().contentType(MediaType.APPLICATION_JSON).body(Map.of(
                    "to", token.token(), "title", notification.getTitle(), "body", notification.getContent(),
                    "data", Map.of("type", notification.getType().name(), "referenceId", notification.getReferenceId() == null ? "" : notification.getReferenceId(), "referenceType", notification.getReferenceType() == null ? "" : notification.getReferenceType())
            )).retrieve().body(String.class);
            if (body != null && (body.contains("DeviceNotRegistered") || body.contains("InvalidCredentials"))) return PushResult.INVALID_TOKEN;
            return PushResult.SENT;
        } catch (RestClientResponseException exception) {
            String body = exception.getResponseBodyAsString();
            return body.contains("DeviceNotRegistered") || body.contains("InvalidCredentials") ? PushResult.INVALID_TOKEN : PushResult.FAILED;
        } catch (RuntimeException exception) { return PushResult.FAILED; }
    }
}
