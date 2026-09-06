package com.notificationservice.application;

import com.notificationservice.application.port.out.AudienceResolver;
import com.notificationservice.application.port.out.RealtimeNotifier;
import com.notificationservice.application.service.ScheduledNotificationService;
import com.notificationservice.domain.model.ScheduledNotification;
import com.notificationservice.domain.model.ScheduledStatus;
import com.notificationservice.domain.repository.NotificationRepository;
import com.notificationservice.domain.repository.ScheduledNotificationRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ScheduledNotificationServiceTest {
    @Test
    void emptyResolvedAudienceMarksScheduleFailedInsteadOfCompleted() {
        ScheduledNotificationRepository schedules = mock(ScheduledNotificationRepository.class);
        NotificationRepository notifications = mock(NotificationRepository.class);
        AudienceResolver audience = mock(AudienceResolver.class);
        RealtimeNotifier realtime = mock(RealtimeNotifier.class);
        Instant now = Instant.parse("2026-09-06T00:00:00Z");
        ScheduledNotification pending = ScheduledNotification.pending("USER", null, "Title", "Content",
                now.minusSeconds(1), UUID.randomUUID(), now.minusSeconds(60));
        when(schedules.findDue(any(), eq(50))).thenReturn(List.of(pending));
        when(schedules.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(audience.resolve("USER", null)).thenReturn(List.of());
        var service = new ScheduledNotificationService(schedules, notifications, audience, realtime,
                Clock.fixed(now, ZoneOffset.UTC));

        service.dispatchDue();

        var saved = org.mockito.ArgumentCaptor.forClass(ScheduledNotification.class);
        verify(schedules, times(2)).save(saved.capture());
        assertThat(saved.getAllValues().getLast().getStatus()).isEqualTo(ScheduledStatus.FAILED);
        verifyNoInteractions(notifications, realtime);
    }
}
