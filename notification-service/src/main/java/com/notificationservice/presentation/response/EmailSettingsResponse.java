package com.notificationservice.presentation.response;

import com.notificationservice.domain.model.EmailSecurity;

public record EmailSettingsResponse(
        String smtpHost,
        int smtpPort,
        String smtpUsername,
        boolean passwordConfigured,
        String fromEmail,
        String fromName,
        EmailSecurity security,
        boolean enabled) {
}
