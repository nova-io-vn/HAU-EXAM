package com.notificationservice.application.service;

import com.notificationservice.application.dto.IncomingEvent;
import com.notificationservice.application.port.in.EventNotificationUseCase;
import com.notificationservice.application.port.out.DeviceTokenRepository;
import com.notificationservice.application.port.out.EmailSender;
import com.notificationservice.application.port.out.ProcessedEventStore;
import com.notificationservice.application.port.out.PushProvider;
import com.notificationservice.application.port.out.RealtimeNotifier;
import com.notificationservice.domain.model.Notification;
import com.notificationservice.domain.model.NotificationType;
import com.notificationservice.domain.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class EventNotificationService implements EventNotificationUseCase {
    private final NotificationRepository notifications;
    private final DeviceTokenRepository deviceTokens;
    private final ProcessedEventStore inbox;
    private final RealtimeNotifier realtime;
    private final PushProvider push;
    private final EmailSender email;
    private final Clock clock;

    public EventNotificationService(NotificationRepository notifications, DeviceTokenRepository deviceTokens,
                                    ProcessedEventStore inbox, RealtimeNotifier realtime, PushProvider push,
                                    EmailSender email, Clock clock) {
        this.notifications = notifications;
        this.deviceTokens = deviceTokens;
        this.inbox = inbox;
        this.realtime = realtime;
        this.push = push;
        this.email = email;
        this.clock = clock;
    }

    @Transactional
    public boolean handle(IncomingEvent event) {
        if (inbox.exists(event.eventId())) return false;
        NotificationType type = type(event.eventType());
        Map<String, Object> payload = event.payload() == null ? Map.of() : event.payload();

        if (type == NotificationType.PASSWORD_RESET_OTP) {
            email.send(required(payload, "email"), "Password reset OTP", required(payload, "otp"));
        } else {
            UUID recipient = recipient(type, payload);
            Notification notification = notifications.save(Notification.unread(recipient, type, title(type),
                    content(type, payload), string(payload, "referenceId"), string(payload, "referenceType"),
                    Instant.now(clock)));
            realtime.send(notification);
            deviceTokens.findActiveByUser(recipient).forEach(token -> {
                if (push.send(token, notification) == PushProvider.PushResult.INVALID_TOKEN) {
                    deviceTokens.save(token.deactivate(Instant.now(clock)));
                }
            });
            if ((type == NotificationType.USER_APPROVED || type == NotificationType.USER_REJECTED)
                    && payload.get("email") != null) {
                email.send(string(payload, "email"), title(type), content(type, payload));
            }
        }

        inbox.record(event.eventId(), event.eventType(), Instant.now(clock));
        return true;
    }

    private UUID recipient(NotificationType type, Map<String, Object> payload) {
        String field = switch (type) {
            case USER_APPROVED, USER_REJECTED -> "recipientUserId";
            case QUESTION_SUBMITTED, QUESTION_APPROVED, QUESTION_REJECTED, QUESTION_REVISION_REQUESTED -> "createdBy";
            case AI_GENERATION_COMPLETED, AI_GENERATION_FAILED -> "requestedBy";
            case EXAM_GENERATED -> payload.containsKey("requestedBy") ? "requestedBy" : "createdBy";
            default -> throw new IllegalArgumentException("No recipient contract for event type " + type);
        };
        try {
            return UUID.fromString(required(payload, field));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid event recipient field: " + field, exception);
        }
    }

    private NotificationType type(String eventType) {
        return switch (eventType) {
            case "PASSWORD_RESET_OTP_REQUESTED" -> NotificationType.PASSWORD_RESET_OTP;
            case "USER_APPROVED" -> NotificationType.USER_APPROVED;
            case "USER_REJECTED" -> NotificationType.USER_REJECTED;
            case "QUESTION_SUBMITTED" -> NotificationType.QUESTION_SUBMITTED;
            case "QUESTION_APPROVED" -> NotificationType.QUESTION_APPROVED;
            case "QUESTION_REJECTED" -> NotificationType.QUESTION_REJECTED;
            case "QUESTION_REVISION_REQUESTED" -> NotificationType.QUESTION_REVISION_REQUESTED;
            case "AI_GENERATION_COMPLETED" -> NotificationType.AI_GENERATION_COMPLETED;
            case "AI_GENERATION_FAILED" -> NotificationType.AI_GENERATION_FAILED;
            case "EXAM_GENERATED" -> NotificationType.EXAM_GENERATED;
            default -> throw new IllegalArgumentException("Unsupported event type");
        };
    }

    private String title(NotificationType type) {
        return switch (type) {
            case USER_APPROVED -> "Registration approved";
            case USER_REJECTED -> "Registration rejected";
            case QUESTION_SUBMITTED -> "Question submitted";
            case QUESTION_APPROVED -> "Question approved";
            case QUESTION_REJECTED -> "Question rejected";
            case QUESTION_REVISION_REQUESTED -> "Question revision requested";
            case AI_GENERATION_COMPLETED -> "AI generation completed";
            case AI_GENERATION_FAILED -> "AI generation failed";
            case EXAM_GENERATED -> "Exam generated";
            default -> "Notification";
        };
    }

    private String content(NotificationType type, Map<String, Object> payload) {
        Object message = payload.get("message");
        return message == null ? title(type) : String.valueOf(message);
    }

    private String required(Map<String, Object> payload, String key) {
        String value = string(payload, key);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Missing event field: " + key);
        return value;
    }

    private String string(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value == null ? null : String.valueOf(value);
    }
}
