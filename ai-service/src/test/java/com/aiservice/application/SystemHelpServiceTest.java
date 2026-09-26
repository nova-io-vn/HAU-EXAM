package com.aiservice.application;

import com.aiservice.application.port.out.AiProvider;
import com.aiservice.application.service.SystemHelpService;
import com.aiservice.domain.exception.ProviderException;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SystemHelpServiceTest {
    @Test
    void providerFailureFallsBackToLocalRoleGuide() {
        AiProvider provider = mock(AiProvider.class);
        when(provider.systemHelp(anyString(), anyString())).thenThrow(new ProviderException("offline", true, null));
        var service = new SystemHelpService(provider, new ObjectMapper(), null);

        var result = service.ask("USER", "Hướng dẫn tạo câu hỏi");

        assertTrue(result.answer().contains("Tạo câu hỏi"));
        assertFalse(result.actions().isEmpty());
    }
}
