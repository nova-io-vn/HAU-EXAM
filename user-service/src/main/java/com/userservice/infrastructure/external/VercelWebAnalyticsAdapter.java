package com.userservice.infrastructure.external;

import com.userservice.application.exception.WebAnalyticsException;
import com.userservice.application.port.out.WebAnalyticsPort;
import com.userservice.infrastructure.config.VercelAnalyticsProperties;
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
    private final RestClient client;

    public VercelWebAnalyticsAdapter(VercelAnalyticsProperties properties) {
        this.properties = properties;
        this.client = RestClient.builder().baseUrl(properties.resolvedApiUrl()).build();
    }

    @Override
    public boolean configured() {
        return properties.configured();
    }

    @Override
    public List<DailyTraffic> dailyTraffic(LocalDate since, LocalDate until) {
        if (!configured()) return List.of();
        try {
            VercelResponse response = client.get()
                    .uri(builder -> {
                        var uri = builder.path("/v1/query/web-analytics/visits/aggregate")
                                .queryParam("projectId", properties.projectId())
                                .queryParam("by", "day")
                                .queryParam("since", since)
                                .queryParam("until", until);
                        if (properties.teamId() != null && !properties.teamId().isBlank()) {
                            uri.queryParam("teamId", properties.teamId());
                        }
                        return uri.build();
                    })
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.token())
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

    public record VercelResponse(List<VercelRow> data) { }
    public record VercelRow(Instant timestamp, long pageviews, long visitors) { }
}
