package com.notificationservice.presentation.controller;

import com.notificationservice.application.service.SupportChatService;
import java.util.UUID;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;

@Controller
public class SupportTypingController {
    private final SupportChatService service;

    public SupportTypingController(SupportChatService service) {
        this.service = service;
    }

    @MessageMapping("/support/typing")
    public void typing(TypingMessage message, JwtAuthenticationToken authentication) {
        if (message == null || message.conversationId() == null || authentication == null) return;
        var jwt = authentication.getToken();
        service.typing(message.conversationId(), UUID.fromString(jwt.getSubject()),
                jwt.getClaimAsString("role"), jwt.getClaimAsString("facultyId"), message.typing());
    }

    public record TypingMessage(UUID conversationId, boolean typing) { }
}
