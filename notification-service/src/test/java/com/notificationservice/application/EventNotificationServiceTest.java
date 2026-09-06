package com.notificationservice.application;

import com.notificationservice.application.dto.IncomingEvent;
import com.notificationservice.application.port.out.*;
import com.notificationservice.application.service.EventNotificationService;
import com.notificationservice.domain.repository.*;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class EventNotificationServiceTest {
    @Test void otpUsesEmailOnlyAndDuplicateIsSkipped() {
        var repository = mock(NotificationRepository.class); var tokens = mock(DeviceTokenRepository.class); var inbox = mock(ProcessedEventStore.class); var ws = mock(RealtimeNotifier.class); var push = mock(PushProvider.class); var mail = mock(EmailSender.class);
        UUID id = UUID.randomUUID(); var service = new EventNotificationService(repository, tokens, inbox, ws, push, mail, Clock.systemUTC());
        var event = new IncomingEvent(id, "PASSWORD_RESET_OTP_REQUESTED", UUID.randomUUID(), Map.of("email", "u@hau.edu.vn", "otp", "123456"));
        assertThat(service.handle(event)).isTrue(); verify(mail).send("u@hau.edu.vn", "Password reset OTP", "123456"); verifyNoInteractions(repository, tokens, ws, push);
        when(inbox.exists(id)).thenReturn(true); assertThat(service.handle(event)).isFalse(); verify(mail, times(1)).send(any(), any(), any());
    }

    @Test void questionApprovalPersistsBeforeRealtime() {
        var repository = mock(NotificationRepository.class); var tokens = mock(DeviceTokenRepository.class); var inbox = mock(ProcessedEventStore.class); var ws = mock(RealtimeNotifier.class); var push = mock(PushProvider.class); var mail = mock(EmailSender.class);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0)); when(tokens.findActiveByUser(any())).thenReturn(List.of()); var order = inOrder(repository, ws);
        var service = new EventNotificationService(repository, tokens, inbox, ws, push, mail, Clock.systemUTC());
        service.handle(new IncomingEvent(UUID.randomUUID(), "QUESTION_APPROVED", UUID.randomUUID(), Map.of("createdBy", UUID.randomUUID().toString())));
        order.verify(repository).save(any()); order.verify(ws).send(any()); verifyNoInteractions(mail, push);
    }

    @Test void mapsAiAndUserRecipientsUsingTheirOwnContracts() {
        var repository = mock(NotificationRepository.class); var tokens = mock(DeviceTokenRepository.class); var inbox = mock(ProcessedEventStore.class); var ws = mock(RealtimeNotifier.class); var push = mock(PushProvider.class); var mail = mock(EmailSender.class);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0)); when(tokens.findActiveByUser(any())).thenReturn(List.of());
        var service = new EventNotificationService(repository, tokens, inbox, ws, push, mail, Clock.systemUTC());
        UUID aiUser = UUID.randomUUID(), applicant = UUID.randomUUID();
        service.handle(new IncomingEvent(UUID.randomUUID(), "AI_GENERATION_COMPLETED", UUID.randomUUID(), Map.of("requestedBy", aiUser.toString())));
        service.handle(new IncomingEvent(UUID.randomUUID(), "USER_APPROVED", UUID.randomUUID(), Map.of("recipientUserId", applicant.toString())));
        var saved = org.mockito.ArgumentCaptor.forClass(com.notificationservice.domain.model.Notification.class);
        verify(repository, times(2)).save(saved.capture());
        assertThat(saved.getAllValues()).extracting(com.notificationservice.domain.model.Notification::userId)
                .containsExactly(aiUser, applicant);
    }
}
