package com.notificationservice.application.service;

import com.notificationservice.infrastructure.mail.SecretProtector;
import com.notificationservice.infrastructure.persistence.entity.TelegramLinkTokenEntity;
import com.notificationservice.infrastructure.persistence.repository.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.client.RestClient;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TelegramNotificationServiceTest {
    @Test void createsHashedOneTimeDeepLinkWithoutExposingBotToken(){
        var bots=mock(JpaTelegramBotSettingsRepository.class);var connections=mock(JpaTelegramConnectionRepository.class);var tokens=mock(JpaTelegramLinkTokenRepository.class);var preferences=mock(JpaTelegramPreferenceRepository.class);var protector=mock(SecretProtector.class);var builder=mock(RestClient.Builder.class);when(builder.build()).thenReturn(mock(RestClient.class));when(bots.findAll()).thenReturn(List.of());
        var service=new TelegramNotificationService(bots,connections,tokens,preferences,protector,builder,"super-secret-bot-token","hau_qm_bot","https://api.telegram.org","https://hau.example","webhook-secret");
        var link=service.createLink(UUID.randomUUID());var saved=ArgumentCaptor.forClass(TelegramLinkTokenEntity.class);verify(tokens).save(saved.capture());
        assertTrue(link.url().startsWith("https://t.me/hau_qm_bot?start="));assertFalse(link.url().contains("super-secret-bot-token"));assertFalse(link.url().contains(saved.getValue().getTokenHash()));assertEquals(64,saved.getValue().getTokenHash().length());assertNull(saved.getValue().getConsumedAt());
    }
}
