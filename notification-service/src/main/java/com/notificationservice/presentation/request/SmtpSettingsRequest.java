package com.notificationservice.presentation.request;

import com.notificationservice.domain.model.EmailSecurity;
import jakarta.validation.constraints.*;

public record SmtpSettingsRequest(
        @Size(max = 255) String smtpHost,
        @Min(1) @Max(65535) Integer smtpPort,
        @Size(max = 254) String smtpUsername,
        @Size(max = 512) String smtpPassword,
        @Email @Size(max = 254) String fromEmail,
        @Size(max = 160) String fromName,
        EmailSecurity security,
        Boolean enabled) {
}
