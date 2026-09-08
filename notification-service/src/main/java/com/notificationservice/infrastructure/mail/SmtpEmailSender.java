package com.notificationservice.infrastructure.mail;

import com.notificationservice.application.port.out.EmailSender;
import com.notificationservice.application.service.EmailSettingsService;
import org.springframework.stereotype.Component;

@Component
public class SmtpEmailSender implements EmailSender {
    private final EmailSettingsService settings;

    public SmtpEmailSender(EmailSettingsService settings) { this.settings = settings; }

    @Override
    public void send(String recipient, String subject, String content) {
        settings.send(recipient, subject, content);
    }
}
