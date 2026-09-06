package com.authservice.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "auth.jwt")
public class JwtProperties {
    private String privateKeyBase64;
    private String publicKeyBase64;
    private boolean generateEphemeralKey;
    private String keyId = "hau-exam-auth-key";
    private String issuer = "hau-exam-auth";
    private Duration accessTokenTtl = Duration.ofMinutes(15);
    private Duration refreshTokenTtl = Duration.ofDays(30);
    public String getPrivateKeyBase64() { return privateKeyBase64; }
    public void setPrivateKeyBase64(String value) { privateKeyBase64 = value; }
    public String getPublicKeyBase64() { return publicKeyBase64; }
    public void setPublicKeyBase64(String value) { publicKeyBase64 = value; }
    public boolean isGenerateEphemeralKey() { return generateEphemeralKey; }
    public void setGenerateEphemeralKey(boolean value) { generateEphemeralKey = value; }
    public String getKeyId() { return keyId; }
    public void setKeyId(String value) { keyId = value; }
    public String getIssuer() { return issuer; }
    public void setIssuer(String value) { issuer = value; }
    public Duration getAccessTokenTtl() { return accessTokenTtl; } public void setAccessTokenTtl(Duration value) { accessTokenTtl = value; }
    public Duration getRefreshTokenTtl() { return refreshTokenTtl; } public void setRefreshTokenTtl(Duration value) { refreshTokenTtl = value; }
}
