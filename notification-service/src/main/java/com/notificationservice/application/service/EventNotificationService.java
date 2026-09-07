package com.notificationservice.application.service;

import com.notificationservice.application.dto.IncomingEvent;
import com.notificationservice.application.dto.UserContact;
import com.notificationservice.application.port.in.EventNotificationUseCase;
import com.notificationservice.application.port.out.*;
import com.notificationservice.domain.model.Notification;
import com.notificationservice.domain.model.NotificationType;
import com.notificationservice.domain.repository.DeviceTokenRepository;
import com.notificationservice.domain.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class EventNotificationService implements EventNotificationUseCase {
    private final NotificationRepository notifications; private final DeviceTokenRepository deviceTokens;
    private final ProcessedEventStore inbox; private final RealtimeNotifier realtime; private final PushProvider push;
    private final EmailSender email; private final Clock clock; private final UserContactResolver contacts;
    public EventNotificationService(NotificationRepository notifications, DeviceTokenRepository deviceTokens, ProcessedEventStore inbox,
                                    RealtimeNotifier realtime, PushProvider push, EmailSender email, Clock clock, UserContactResolver contacts) {
        this.notifications=notifications; this.deviceTokens=deviceTokens; this.inbox=inbox; this.realtime=realtime; this.push=push; this.email=email; this.clock=clock; this.contacts=contacts;
    }
    @Transactional public boolean handle(IncomingEvent event) {
        if (inbox.exists(event.eventId())) return false;
        NotificationType type=type(event.eventType()); Map<String,Object> payload=event.payload()==null?Map.of():event.payload();
        if(type==NotificationType.PASSWORD_RESET_OTP) email.send(required(payload,"email"),"Password reset OTP",required(payload,"otp"));
        else {
            UserContact contact=isQuestionEmail(type)?contacts.resolve(recipient(type,payload)):null; UUID recipient=recipient(type,payload);
            Notification notification=notifications.save(Notification.unread(recipient,type,title(type),content(type,payload),string(payload,"referenceId"),string(payload,"referenceType"),Instant.now(clock)));
            realtime.send(notification);
            deviceTokens.findActiveByUser(recipient).forEach(token->{if(push.send(token,notification)==PushProvider.PushResult.INVALID_TOKEN) deviceTokens.save(token.deactivate(Instant.now(clock)));});
            if((type==NotificationType.USER_APPROVED||type==NotificationType.USER_REJECTED)&&payload.get("email")!=null) email.send(string(payload,"email"),title(type),content(type,payload));
            if(contact!=null&&contact.email()!=null&&!contact.email().isBlank()) email.send(contact.email(),title(type),questionEmailContent(type,payload,contact));
        }
        inbox.record(event.eventId(),event.eventType(),Instant.now(clock)); return true;
    }
    private UUID recipient(NotificationType type,Map<String,Object> payload){String field=switch(type){case USER_APPROVED,USER_REJECTED,USER_ROLE_CHANGED,USER_FACULTY_CHANGED,USER_STATUS_CHANGED->"recipientUserId";case QUESTION_SUBMITTED,QUESTION_APPROVED,QUESTION_REJECTED,QUESTION_REVISION_REQUESTED->payload.containsKey("authorUserId")?"authorUserId":"createdBy";case AI_GENERATION_COMPLETED,AI_GENERATION_FAILED->"requestedBy";case EXAM_GENERATED->payload.containsKey("requestedBy")?"requestedBy":"createdBy";default->throw new IllegalArgumentException("No recipient contract for event type "+type);};try{return UUID.fromString(required(payload,field));}catch(IllegalArgumentException e){throw new IllegalArgumentException("Invalid event recipient field: "+field,e);}}
    private NotificationType type(String eventType){return switch(eventType){case "PASSWORD_RESET_OTP_REQUESTED"->NotificationType.PASSWORD_RESET_OTP;case "USER_APPROVED"->NotificationType.USER_APPROVED;case "USER_REJECTED"->NotificationType.USER_REJECTED;case "USER_ROLE_CHANGED"->NotificationType.USER_ROLE_CHANGED;case "USER_FACULTY_CHANGED"->NotificationType.USER_FACULTY_CHANGED;case "USER_STATUS_CHANGED"->NotificationType.USER_STATUS_CHANGED;case "QUESTION_SUBMITTED"->NotificationType.QUESTION_SUBMITTED;case "QUESTION_APPROVED"->NotificationType.QUESTION_APPROVED;case "QUESTION_REJECTED"->NotificationType.QUESTION_REJECTED;case "QUESTION_REVISION_REQUESTED"->NotificationType.QUESTION_REVISION_REQUESTED;case "AI_GENERATION_COMPLETED"->NotificationType.AI_GENERATION_COMPLETED;case "AI_GENERATION_FAILED"->NotificationType.AI_GENERATION_FAILED;case "EXAM_GENERATED"->NotificationType.EXAM_GENERATED;default->throw new IllegalArgumentException("Unsupported event type");};}
    private String title(NotificationType type){return switch(type){case USER_APPROVED->"[Tài khoản HAU QM] Tài khoản của bạn đã được phê duyệt";case USER_REJECTED->"[HAU QM] Thông báo đăng ký tài khoản";case USER_ROLE_CHANGED->"Vai trò tài khoản của bạn đã được cập nhật";case USER_FACULTY_CHANGED->"Đơn vị/Khoa của bạn đã được thay đổi";case USER_STATUS_CHANGED->"Trạng thái tài khoản của bạn đã được cập nhật";case QUESTION_SUBMITTED->"Câu hỏi đã được gửi phê duyệt";case QUESTION_APPROVED->"[HAU QM] Câu hỏi đã được phê duyệt";case QUESTION_REJECTED->"[HAU QM] Câu hỏi đã bị từ chối";case QUESTION_REVISION_REQUESTED->"[HAU QM] Câu hỏi cần chỉnh sửa";case AI_GENERATION_COMPLETED->"AI generation completed";case AI_GENERATION_FAILED->"AI generation failed";case EXAM_GENERATED->"Exam generated";default->"Notification";};}
    private String content(NotificationType type,Map<String,Object> payload){String message=string(payload,"message");if(message!=null)return message;if(type==NotificationType.USER_APPROVED)return "Xin chào "+string(payload,"fullName")+", tài khoản HAU QM System của bạn đã được Quản trị viên hệ thống phê duyệt. Mã giảng viên: "+string(payload,"lecturerCode")+". Khoa: "+value(payload,"facultyName","facultyId")+". Vai trò: "+roleLabel(string(payload,"role"))+". Bạn có thể đăng nhập vào hệ thống.";if(type==NotificationType.USER_REJECTED)return "Xin chào "+string(payload,"fullName")+", yêu cầu đăng ký tài khoản HAU QM System của bạn chưa được chấp thuận."+(string(payload,"reason")==null?"":" Lý do: "+string(payload,"reason"));if(type==NotificationType.USER_ROLE_CHANGED)return "Vai trò tài khoản của bạn đã được cập nhật thành "+roleLabel(string(payload,"role"))+".";if(type==NotificationType.USER_FACULTY_CHANGED)return "Khoa của bạn đã được cập nhật thành "+value(payload,"facultyName","facultyId")+".";return title(type);}
    private String questionEmailContent(NotificationType type,Map<String,Object> payload,UserContact contact){String question=string(payload,"questionId"),subject=string(payload,"subjectId"),reason=string(payload,"reviewComment");String state=switch(type){case QUESTION_APPROVED->"đã được phê duyệt và đưa vào ngân hàng câu hỏi";case QUESTION_REJECTED->"đã bị từ chối";default->"cần được chỉnh sửa trước khi tiếp tục phê duyệt";};StringBuilder body=new StringBuilder("Xin chào ").append(contact.fullName()).append(",\n\n").append("Câu hỏi ").append(question==null?"":question).append(" ").append(state).append(".\n");if(subject!=null)body.append("Môn học: ").append(subject).append("\n");if(reason!=null&&!reason.isBlank())body.append("Phản hồi: ").append(reason).append("\n");return body.append("\nVui lòng đăng nhập HAU QM System để theo dõi và xử lý.").toString();}
    private boolean isQuestionEmail(NotificationType type){return type==NotificationType.QUESTION_APPROVED||type==NotificationType.QUESTION_REJECTED||type==NotificationType.QUESTION_REVISION_REQUESTED;}
    private String required(Map<String,Object> payload,String key){String value=string(payload,key);if(value==null||value.isBlank())throw new IllegalArgumentException("Missing event field: "+key);return value;}
    private String string(Map<String,Object> payload,String key){Object value=payload.get(key);return value==null?null:String.valueOf(value);}
    private String value(Map<String,Object> payload,String preferred,String fallback){return string(payload,preferred)!=null?string(payload,preferred):string(payload,fallback);}
    private String roleLabel(String role){return "SUBJECT_ADMIN".equals(role)?"Quản trị viên chuyên môn":"Giảng viên";}
}
