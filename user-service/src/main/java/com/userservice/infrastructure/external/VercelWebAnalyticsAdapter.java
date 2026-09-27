package com.userservice.infrastructure.external;

import com.userservice.application.exception.WebAnalyticsException;
import com.userservice.application.port.out.WebAnalyticsPort;
import com.userservice.infrastructure.config.VercelAnalyticsProperties;
import com.userservice.infrastructure.service.VercelAnalyticsSettingsService;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Component
public class VercelWebAnalyticsAdapter implements WebAnalyticsPort {
    private final VercelAnalyticsProperties properties;
    private final VercelAnalyticsSettingsService settings;
    private final RestClient client;

    public VercelWebAnalyticsAdapter(VercelAnalyticsProperties properties, VercelAnalyticsSettingsService settings) {
        this.properties = properties; this.settings = settings;
        this.client = RestClient.builder().baseUrl(properties.resolvedApiUrl()).build();
    }

    @Override
    public boolean configured() {
        return settings.current().configured();
    }

    @Override
    public List<DailyTraffic> dailyTraffic(LocalDate since, LocalDate until) {
        if (!configured()) return List.of();
        try {
            var configured = settings.current();
            VercelResponse response = client.get()
                    .uri(builder -> {
                        var uri = builder.path("/v1/query/web-analytics/visits/aggregate")
                                .queryParam("projectId", configured.projectId())
                                .queryParam("by", "day")
                                .queryParam("since", since)
                                .queryParam("until", until);
                        if (configured.teamId() != null && !configured.teamId().isBlank()) {
                            uri.queryParam("teamId", configured.teamId());
                        }
                        return uri.build();
                    })
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + configured.token())
                    .retrieve()
                    .body(VercelResponse.class);
            if (response == null || response.data() == null) return List.of();
            return response.data().stream()
                    .filter(row -> row.timestamp() != null)
                    .map(row -> new DailyTraffic(
                            row.timestamp().atZone(ZoneOffset.UTC).toLocalDate(),
                            Math.max(0, row.visitors()),
                            Math.max(0, row.pageviews())
                    ))
                    .toList();
        } catch (RuntimeException exception) {
            throw new WebAnalyticsException("Không thể tải dữ liệu Vercel Web Analytics", exception);
        }
    }

    @Override
    public String testConnection() {
        if (!configured()) return "NOT_CONFIGURED";
        try {
            dailyTraffic(LocalDate.now(ZoneOffset.UTC).minusDays(1), LocalDate.now(ZoneOffset.UTC));
            return "WORKING";
        } catch (WebAnalyticsException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof org.springframework.web.client.HttpClientErrorException http) {
                return switch (http.getStatusCode().value()) { case 401, 403 -> "FORBIDDEN"; case 404 -> "PROJECT_NOT_FOUND"; case 429 -> "RATE_LIMITED"; default -> "API_ERROR"; };
            }
            return "NETWORK_ERROR";
        }
    }

    public record VercelResponse(List<VercelRow> data) { }
    public record VercelRow(Instant timestamp, long pageviews, long visitors) { }
}
