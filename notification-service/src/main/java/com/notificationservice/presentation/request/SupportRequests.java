package com.notificationservice.presentation.request;
import jakarta.validation.constraints.*;
public final class SupportRequests { private SupportRequests(){}
 public record CreateConversation(@NotBlank @Size(max=200) String subject,@Size(max=5000) String content,java.util.UUID recipientUserId){}
 public record Status(@NotNull com.notificationservice.domain.model.SupportStatus status){}
}
