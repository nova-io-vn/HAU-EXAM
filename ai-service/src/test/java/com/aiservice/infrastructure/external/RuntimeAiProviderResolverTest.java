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

class RuntimeAiProviderResolverTest {
    @Test
    void savedGeminiConfigurationIsUsedAtRuntime() {
        AiSettingsRepository settings = mock(AiSettingsRepository.class);
        AiSecretProtector protector = mock(AiSecretProtector.class);
        GeminiAdapter gemini = mock(GeminiAdapter.class);
        AiSettingsEntity configured = new AiSettingsEntity();
        configured.provider = "GEMINI";
        configured.model = "gemini-custom";
        configured.apiKeyEncrypted = "encrypted";
        when(settings.findAll()).thenReturn(List.of(configured));
        when(protector.decrypt("encrypted")).thenReturn("runtime-key");
        when(gemini.generateQuestions("source", "request", "runtime-key", "gemini-custom")).thenReturn("[]");
        var resolver = new RuntimeAiProviderResolver(settings, protector, gemini, RestClient.builder(), new ObjectMapper(), new MockEnvironment());

        assertEquals("[]", resolver.generateQuestions("source", "request"));
        verify(gemini).generateQuestions("source", "request", "runtime-key", "gemini-custom");
    }
}
