package com.userservice.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vercel.analytics")
public record VercelAnalyticsProperties(
        String apiUrl,
        String token,
        String projectId,
        String teamId
) {
    public boolean configured() {
        return notBlank(token) && notBlank(projectId);
    }

    public String resolvedApiUrl() {
        return notBlank(apiUrl) ? apiUrl : "https://api.vercel.com";
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
