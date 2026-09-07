package com.userservice.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cloudinary")
public record CloudinaryProperties(String cloudName, String apiKey, String apiSecret) {
    public boolean configured() { return notBlank(cloudName) && notBlank(apiKey) && notBlank(apiSecret); }
    private static boolean notBlank(String value) { return value != null && !value.isBlank(); }
}
