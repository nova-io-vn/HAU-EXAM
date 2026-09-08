package com.notificationservice.infrastructure.persistence.entity;

import com.notificationservice.domain.model.EmailSecurity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_settings")
public class EmailSettingsEntity {
    @Id private UUID id;
    @Column(name = "smtp_host", nullable = false, length = 255) private String smtpHost;
    @Column(name = "smtp_port", nullable = false) private int smtpPort;
    @Column(name = "smtp_username", length = 254) private String smtpUsername;
    @Column(name = "smtp_password_encrypted", columnDefinition = "text") private String smtpPasswordEncrypted;
    @Column(name = "from_email", nullable = false, length = 254) private String fromEmail;
    @Column(name = "from_name", nullable = false, length = 160) private String fromName;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private EmailSecurity security;
    @Column(nullable = false) private boolean enabled;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getSmtpHost() { return smtpHost; }
    public void setSmtpHost(String value) { smtpHost = value; }
    public int getSmtpPort() { return smtpPort; }
    public void setSmtpPort(int value) { smtpPort = value; }
    public String getSmtpUsername() { return smtpUsername; }
    public void setSmtpUsername(String value) { smtpUsername = value; }
    public String getSmtpPasswordEncrypted() { return smtpPasswordEncrypted; }
    public void setSmtpPasswordEncrypted(String value) { smtpPasswordEncrypted = value; }
    public String getFromEmail() { return fromEmail; }
    public void setFromEmail(String value) { fromEmail = value; }
    public String getFromName() { return fromName; }
    public void setFromName(String value) { fromName = value; }
    public EmailSecurity getSecurity() { return security; }
    public void setSecurity(EmailSecurity value) { security = value; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant value) { createdAt = value; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant value) { updatedAt = value; }
}
