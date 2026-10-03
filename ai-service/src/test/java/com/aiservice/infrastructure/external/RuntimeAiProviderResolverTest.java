package com.aiservice.infrastructure.external;

import com.aiservice.infrastructure.persistence.entity.AiSettingsEntity;
import com.aiservice.infrastructure.persistence.repository.AiSettingsRepository;
import com.aiservice.infrastructure.security.AiSecretProtector;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.contains;

class RuntimeAiProviderResolverTest {
    @Test
    void savedGeminiConfigurationIsUsedAtRuntime() {
        AiSettingsRepository settings = mock(AiSettingsRepository.class);
        AiSecretProtector protector = mock(AiSecretProtector.class);
        GeminiAdapter gemini = mock(GeminiAdapter.class);
        AiSettingsEntity configured = new AiSettingsEntity();
        configured.provider = "GEMINI";
        configured.model = "gemini-custom";
        configured.persona = "CONCISE";
        configured.apiKeyEncrypted = "encrypted";
        when(settings.findAll()).thenReturn(List.of(configured));
        when(protector.decrypt("encrypted")).thenReturn("runtime-key");
        when(gemini.generateQuestions("source", "request", "runtime-key", "gemini-custom")).thenReturn("[]");
        var resolver = new RuntimeAiProviderResolver(settings, protector, gemini, RestClient.builder(), new ObjectMapper(), new MockEnvironment());

        assertEquals("[]", resolver.generateQuestions("source", "request"));
        verify(gemini).generateQuestions("source", "request", "runtime-key", "gemini-custom");
    }

    @Test
    void savedModelUsesEnvironmentCredentialWhenAdminKeepsExistingKey() {
        AiSettingsRepository settings = mock(AiSettingsRepository.class);
        AiSecretProtector protector = mock(AiSecretProtector.class);
        GeminiAdapter gemini = mock(GeminiAdapter.class);
        AiSettingsEntity configured = new AiSettingsEntity();
        configured.provider = "GEMINI";
        configured.model = "gemini-selected";
        when(settings.findAll()).thenReturn(List.of(configured));
        when(gemini.generateQuestions("source", "request", "environment-key", "gemini-selected")).thenReturn("[]");
        var environment = new MockEnvironment().withProperty("ai.provider.api-key", "environment-key");
        var resolver = new RuntimeAiProviderResolver(settings, protector, gemini, RestClient.builder(), new ObjectMapper(), environment);

        assertEquals("[]", resolver.generateQuestions("source", "request"));
        verify(gemini).generateQuestions("source", "request", "environment-key", "gemini-selected");
    }

    @Test
    void savedPersonaIsAppliedToTheNextKuteRequest() {
        AiSettingsRepository settings = mock(AiSettingsRepository.class);
        AiSecretProtector protector = mock(AiSecretProtector.class);
        GeminiAdapter gemini = mock(GeminiAdapter.class);
        AiSettingsEntity configured = new AiSettingsEntity();
        configured.provider = "GEMINI"; configured.model = "gemini-custom"; configured.apiKeyEncrypted = "encrypted"; configured.persona = "CONCISE";
        when(settings.findAll()).thenReturn(List.of(configured));
        when(protector.decrypt("encrypted")).thenReturn("runtime-key");
        when(gemini.systemHelp(org.mockito.ArgumentMatchers.eq("source"), org.mockito.ArgumentMatchers.eq("request"), org.mockito.ArgumentMatchers.eq("runtime-key"), org.mockito.ArgumentMatchers.eq("gemini-custom"), org.mockito.ArgumentMatchers.anyString())).thenReturn("{}");
        var resolver = new RuntimeAiProviderResolver(settings, protector, gemini, RestClient.builder(), new ObjectMapper(), new MockEnvironment());

        assertEquals("{}", resolver.systemHelp("source", "request"));
        verify(gemini).systemHelp(org.mockito.ArgumentMatchers.eq("source"), org.mockito.ArgumentMatchers.eq("request"), org.mockito.ArgumentMatchers.eq("runtime-key"), org.mockito.ArgumentMatchers.eq("gemini-custom"), contains("extremely concise"));
    }
}
