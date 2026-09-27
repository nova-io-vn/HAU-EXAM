package com.notificationservice.application.service;

import com.notificationservice.application.dto.IncomingEvent;
import com.notificationservice.application.dto.Recipient;
import com.notificationservice.application.dto.UserContact;
import com.notificationservice.application.port.in.EventNotificationUseCase;
import com.notificationservice.application.port.out.*;
import com.notificationservice.domain.model.*;
import com.notificationservice.domain.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.Instant;
import java.util.*;

@Service
public class EventNotificationService implements EventNotificationUseCase {
    private final NotificationRepository notifications;
    private final DeviceTokenRepository deviceTokens;
    private final ProcessedEventStore inbox;
    private final RealtimeNotifier realtime;
    private final PushProvider push;
    private final EmailSender email;
    private final Clock clock;
    private final UserContactResolver contacts;
    private final AudienceResolver audience;
    private final TelegramNotificationService telegram;
    private final EmailTemplateRenderer renderer;

    public EventNotificationService(NotificationRepository n, DeviceTokenRepository d, ProcessedEventStore i,
                                    RealtimeNotifier r, PushProvider p, EmailSender e, Clock c, UserContactResolver u) {
        this(n, d, i, r, p, e, c, u, null, null, new EmailTemplateRenderer());
    }
    public EventNotificationService(NotificationRepository n, DeviceTokenRepository d, ProcessedEventStore i,
                                    RealtimeNotifier r, PushProvider p, EmailSender e, Clock c, UserContactResolver u,
                                    AudienceResolver a, TelegramNotificationService t) {
        this(n, d, i, r, p, e, c, u, a, t, new EmailTemplateRenderer());
    }
    @Autowired
    public EventNotificationService(NotificationRepository n, DeviceTokenRepository d, ProcessedEventStore i,
                                    RealtimeNotifier r, PushProvider p, EmailSender e, Clock c, UserContactResolver u,
                                    AudienceResolver a, TelegramNotificationService t, EmailTemplateRenderer x) {
        notifications=n; deviceTokens=d; inbox=i; realtime=r; push=p; email=e; clock=c; contacts=u; audience=a; telegram=t; renderer=x;
    }

    @Transactional
    public boolean handle(IncomingEvent event) {
        if (inbox.exists(event.eventId())) return false;
        NotificationType type = type(event.eventType());
        Map<String,Object> payload = event.payload() == null ? Map.of() : event.payload();

        if (type == NotificationType.USER_LOGIN_SUCCESS) {
            // Successful logins remain an audit/security event; they are never fanout notifications by default.
            inbox.record(event.eventId(), event.eventType(), Instant.now(clock));
            return true;
        }
        if (type == NotificationType.PASSWORD_RESET_OTP) {
            email.send(required(payload,"email"), "[HAU QM] Mã xác thực đặt lại mật khẩu",
                    renderer.render(string(payload,"fullName"), "Đặt lại mật khẩu", otpBody(payload), "Mở HAU QM", website()));
            inbox.record(event.eventId(), event.eventType(), Instant.now(clock));
            return true;
        }
        if (type == NotificationType.NEW_USER_REGISTERED || type == NotificationType.QUESTION_SUBMITTED) {
            fanout(type, payload);
            if (type == NotificationType.NEW_USER_REGISTERED && payload.get("email") != null) {
                email.send(string(payload,"email"), title(type), renderer.render(string(payload,"fullName"), title(type), content(type,payload), "Xem tài khoản", website()+"/admin/registrations"));
            }
            inbox.record(event.eventId(), event.eventType(), Instant.now(clock));
            return true;
        }
        UUID recipient = recipient(type, payload);
        deliver(recipient, type, payload);
        if ((type == NotificationType.USER_APPROVED || type == NotificationType.USER_REJECTED) && payload.get("email") != null) {
            email.send(string(payload,"email"), title(type), renderer.render(string(payload,"fullName"), title(type), content(type,payload), "Đăng nhập HAU QM", website()+"/login"));
        }
        if (isQuestionEmail(type) && contacts != null) {
            UserContact contact = contacts.resolve(recipient);
            if (contact != null && contact.email() != null && !contact.email().isBlank()) {
                email.send(contact.email(), title(type), renderer.render(contact.fullName(), title(type), questionBody(type,payload,contact), "Xem câu hỏi", website()+"/questions/"+string(payload,"questionId")));
            }
        }
        inbox.record(event.eventId(), event.eventType(), Instant.now(clock));
        return true;
    }

    private void fanout(NotificationType type, Map<String,Object> p) {
        if (audience == null) return;
        String role = type == NotificationType.QUESTION_SUBMITTED ? "SUBJECT_ADMIN" : "SYSTEM_ADMIN";
        String faculty = type == NotificationType.QUESTION_SUBMITTED ? string(p,"facultyId") : null;
        for (Recipient r : audience.resolve(role, faculty)) {
            deliver(r.userId(), type, p);
            if (r.email() != null && !r.email().isBlank()) email.send(r.email(), title(type), renderer.render(string(p,"fullName"), title(type), content(type,p), "Mở HAU QM", website()));
        }
    }

    private void deliver(UUID user, NotificationType type, Map<String,Object> p) {
        Notification n = notifications.save(Notification.unread(user, type, title(type), content(type,p), referenceId(type,p), referenceType(type,p), Instant.now(clock)));
        realtime.send(n);
        deviceTokens.findActiveByUser(user).forEach(t -> { if (push.send(t,n) == PushProvider.PushResult.INVALID_TOKEN) deviceTokens.save(t.deactivate(Instant.now(clock))); });
        if (telegram != null && telegram.enabled(user) && telegramAllowed(user,type)) try { telegram.send(user, telegramBody(type,p)); } catch (Exception ignored) { }
    }
    private boolean telegramAllowed(UUID u, NotificationType t) {
        var p = telegram.preferences(u);
        return switch (t) {
            case NEW_USER_REGISTERED -> p.newUsers();
            case QUESTION_SUBMITTED -> p.questionPending();
            case USER_APPROVED, USER_REJECTED, USER_ROLE_CHANGED, USER_FACULTY_CHANGED, USER_STATUS_CHANGED -> p.actionable();
            default -> p.systemEvents();
        };
    }
    private UUID recipient(NotificationType t, Map<String,Object> p) {
        String field = switch (t) {
            case USER_APPROVED, USER_REJECTED, USER_ROLE_CHANGED, USER_FACULTY_CHANGED, USER_STATUS_CHANGED -> "recipientUserId";
            case QUESTION_APPROVED, QUESTION_REJECTED, QUESTION_REVISION_REQUESTED -> p.containsKey("authorUserId") ? "authorUserId" : "createdBy";
            case AI_GENERATION_COMPLETED, AI_GENERATION_FAILED -> "requestedBy";
            case EXAM_GENERATED -> p.containsKey("requestedBy") ? "requestedBy" : "createdBy";
            default -> throw new IllegalArgumentException("No recipient contract");
        };
        return UUID.fromString(required(p, field));
    }
    private NotificationType type(String x) { return switch (x) {
        case "PASSWORD_RESET_OTP_REQUESTED" -> NotificationType.PASSWORD_RESET_OTP;
        case "USER_REGISTRATION_REQUESTED", "ACCOUNT_REGISTERED", "ACCOUNT_PENDING_APPROVAL" -> NotificationType.NEW_USER_REGISTERED;
        case "USER_LOGIN_SUCCESS" -> NotificationType.USER_LOGIN_SUCCESS;
        case "USER_APPROVED", "ACCOUNT_APPROVED" -> NotificationType.USER_APPROVED;
        case "USER_REJECTED", "ACCOUNT_REJECTED" -> NotificationType.USER_REJECTED;
        case "USER_ROLE_CHANGED" -> NotificationType.USER_ROLE_CHANGED;
        case "USER_FACULTY_CHANGED" -> NotificationType.USER_FACULTY_CHANGED;
        case "USER_STATUS_CHANGED" -> NotificationType.USER_STATUS_CHANGED;
        case "QUESTION_SUBMITTED" -> NotificationType.QUESTION_SUBMITTED;
        case "QUESTION_APPROVED" -> NotificationType.QUESTION_APPROVED;
        case "QUESTION_REJECTED" -> NotificationType.QUESTION_REJECTED;
        case "QUESTION_REVISION_REQUESTED" -> NotificationType.QUESTION_REVISION_REQUESTED;
        case "AI_GENERATION_COMPLETED" -> NotificationType.AI_GENERATION_COMPLETED;
        case "AI_GENERATION_FAILED" -> NotificationType.AI_GENERATION_FAILED;
        case "EXAM_GENERATED" -> NotificationType.EXAM_GENERATED;
        default -> throw new IllegalArgumentException("Unsupported event type");
    };}
    private String title(NotificationType t) { return switch (t) {
        case NEW_USER_REGISTERED -> "Người dùng mới đăng ký";
        case USER_LOGIN_SUCCESS -> "Đăng nhập mới";
        case USER_APPROVED -> "[HAU QM] Tài khoản của bạn đã được phê duyệt";
        case USER_REJECTED -> "[HAU QM] Thông báo đăng ký tài khoản";
        case USER_ROLE_CHANGED -> "Vai trò tài khoản đã được cập nhật";
        case USER_FACULTY_CHANGED -> "Khoa của tài khoản đã được cập nhật";
        case USER_STATUS_CHANGED -> "Trạng thái tài khoản đã được cập nhật";
        case QUESTION_SUBMITTED -> "Câu hỏi mới đang chờ phê duyệt";
        case QUESTION_APPROVED -> "[HAU QM] Câu hỏi của bạn đã được phê duyệt";
        case QUESTION_REJECTED -> "[HAU QM] Câu hỏi của bạn đã bị từ chối";
        case QUESTION_REVISION_REQUESTED -> "[HAU QM] Câu hỏi cần chỉnh sửa";
        case PASSWORD_RESET_OTP -> "[HAU QM] Mã xác thực đặt lại mật khẩu";
        default -> "HAU QM - Thông báo hệ thống";
    };}
    private String content(NotificationType t, Map<String,Object> p) { String name=string(p,"fullName"), code=string(p,"lecturerCode"); return switch(t) {
        case NEW_USER_REGISTERED -> (name==null?"Người dùng":name)+" ("+code+") đã đăng ký tài khoản và đang chờ phê duyệt.";
        case QUESTION_SUBMITTED -> (name==null?"Người dùng":name)+" vừa gửi một câu hỏi thuộc môn "+string(p,"subjectId")+" để phê duyệt.";
        case USER_APPROVED -> "Xin chào "+name+", tài khoản HAU QM của bạn đã được quản trị viên phê duyệt.";
        case USER_REJECTED -> "Xin chào "+name+", yêu cầu đăng ký tài khoản HAU QM chưa được chấp thuận.";
        default -> string(p,"message") == null ? title(t) : string(p,"message");
    };}
    private String telegramBody(NotificationType t, Map<String,Object> p) { return "HAU QM\n━━━━━━━━━━━━\n\n"+content(t,p)+"\n\nXem tại:\n"+website(); }
    private String otpBody(Map<String,Object> p) { return "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản HAU QM của bạn.\n\nMã xác thực:  "+required(p,"otp")+"\n\nMã có hiệu lực trong 5 phút. Nếu bạn không thực hiện yêu cầu này, hãy bỏ qua email."; }
    private String questionBody(NotificationType t, Map<String,Object> p, UserContact c) { return "Câu hỏi "+string(p,"questionId")+" của bạn đã được cập nhật trạng thái. Vui lòng mở HAU QM để xem chi tiết."; }
    private boolean isQuestionEmail(NotificationType t) { return t==NotificationType.QUESTION_APPROVED || t==NotificationType.QUESTION_REJECTED || t==NotificationType.QUESTION_REVISION_REQUESTED; }
    private String referenceId(NotificationType t, Map<String,Object> p) { return t==NotificationType.NEW_USER_REGISTERED ? null : string(p,"questionId"); }
    private String referenceType(NotificationType t, Map<String,Object> p) { return t==NotificationType.NEW_USER_REGISTERED ? "PENDING_USERS" : t==NotificationType.QUESTION_SUBMITTED || isQuestionEmail(t) ? "QUESTION" : null; }
    private String website() { return "https://exam.nova.io.vn"; }
    private String required(Map<String,Object> p,String k){String v=string(p,k);if(v==null||v.isBlank())throw new IllegalArgumentException("Missing event field: "+k);return v;}
    private String string(Map<String,Object> p,String k){Object v=p.get(k);return v==null?null:String.valueOf(v);}
}
