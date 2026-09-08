package com.notificationservice.application.service;

import com.notificationservice.application.exception.EmailDeliveryException;
import com.notificationservice.application.exception.EmailSettingsException;
import com.notificationservice.domain.model.EmailSecurity;
import com.notificationservice.infrastructure.mail.SecretProtector;
import com.notificationservice.infrastructure.persistence.entity.EmailSettingsEntity;
import com.notificationservice.infrastructure.persistence.repository.JpaEmailSettingsRepository;
import com.notificationservice.presentation.request.SmtpSettingsRequest;
import com.notificationservice.presentation.response.EmailSettingsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import java.net.ConnectException;
import java.time.Instant;
import java.util.Properties;

@Service
public class EmailSettingsService {
    private final JpaEmailSettingsRepository repository;
    private final SecretProtector protector;
    private final String envHost, envUsername, envPassword, envFrom, envFromName;
    private final int envPort;
    private final boolean envAuth, envStartTls, envStartTlsRequired;

    public EmailSettingsService(JpaEmailSettingsRepository repository, SecretProtector protector,
            @Value("${spring.mail.host:}") String envHost, @Value("${spring.mail.port:587}") int envPort,
            @Value("${spring.mail.username:}") String envUsername, @Value("${spring.mail.password:}") String envPassword,
            @Value("${notification.mail-from:}") String envFrom, @Value("${notification.mail-from-name:HAU QM}") String envFromName,
            @Value("${spring.mail.properties.mail.smtp.auth:false}") boolean envAuth,
            @Value("${spring.mail.properties.mail.smtp.starttls.enable:false}") boolean envStartTls,
            @Value("${spring.mail.properties.mail.smtp.starttls.required:false}") boolean envStartTlsRequired) {
        this.repository = repository; this.protector = protector; this.envHost = envHost; this.envPort = envPort;
        this.envUsername = envUsername; this.envPassword = envPassword; this.envFrom = envFrom; this.envFromName = envFromName;
        this.envAuth = envAuth; this.envStartTls = envStartTls; this.envStartTlsRequired = envStartTlsRequired;
    }

    @Transactional(readOnly = true)
    public EmailSettingsResponse get() {
        return repository.findAll().stream().findFirst().map(this::response).orElseGet(this::fallbackResponse);
    }

    @Transactional
    public EmailSettingsResponse update(SmtpSettingsRequest request) {
        boolean enabled = Boolean.TRUE.equals(request.enabled());
        String host = trim(request.smtpHost()), username = trim(request.smtpUsername()), from = trim(request.fromEmail()), fromName = trim(request.fromName());
        int port = request.smtpPort() == null ? 587 : request.smtpPort();
        EmailSecurity security = request.security() == null ? EmailSecurity.STARTTLS : request.security();
        if (enabled && (host.isBlank() || from.isBlank())) throw invalid("Cấu hình SMTP chưa hợp lệ.");
        if (enabled && !isEmail(from)) throw invalid("Email người gửi không hợp lệ.");
        if (enabled && (security != EmailSecurity.NONE) && username.isBlank()) throw invalid("Email / tài khoản SMTP là bắt buộc.");
        EmailSettingsEntity entity = repository.findAll().stream().findFirst().orElseGet(() -> {
            EmailSettingsEntity e = new EmailSettingsEntity(); e.setId(java.util.UUID.randomUUID()); e.setCreatedAt(Instant.now()); return e;
        });
        boolean hasOldPassword = entity.getSmtpPasswordEncrypted() != null && !entity.getSmtpPasswordEncrypted().isBlank();
        if (enabled && !hasOldPassword && trim(request.smtpPassword()).isBlank() && security != EmailSecurity.NONE)
            throw invalid("Mật khẩu SMTP là bắt buộc khi bật gửi email lần đầu.");
        entity.setSmtpHost(host); entity.setSmtpPort(port); entity.setSmtpUsername(username); entity.setFromEmail(from);
        entity.setFromName(fromName.isBlank() ? "HAU QM" : fromName); entity.setSecurity(security); entity.setEnabled(enabled); entity.setUpdatedAt(Instant.now());
        if (!trim(request.smtpPassword()).isBlank()) entity.setSmtpPasswordEncrypted(protector.encrypt(request.smtpPassword().trim()));
        return response(repository.save(entity));
    }

    public void sendTest(String recipient) { send(recipient, "[HAU QM] Kiểm tra cấu hình email", "Xin chào,\n\nĐây là email kiểm tra cấu hình gửi thư của hệ thống HAU QM.\n\nNếu bạn nhận được email này, cấu hình SMTP đang hoạt động bình thường.\n\nHAU QM System"); }

    public void send(String recipient, String subject, String content) {
        MailConfig config = activeConfig();
        if (!config.enabled()) throw new EmailDeliveryException("EMAIL_DELIVERY_DISABLED", "Gửi email hiện đang bị tắt.");
        JavaMailSenderImpl sender = createSender(config);
        try {
            var message = sender.createMimeMessage();
            var helper = new org.springframework.mail.javamail.MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(new InternetAddress(config.fromEmail(), config.fromName(), "UTF-8")); helper.setTo(recipient); helper.setSubject(subject); helper.setText(content, false);
            sender.send(message);
        } catch (Exception ex) { throw deliveryException(ex); }
    }

    private JavaMailSenderImpl createSender(MailConfig c) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl(); sender.setHost(c.host()); sender.setPort(c.port()); sender.setUsername(c.username()); sender.setPassword(c.password());
        Properties p = sender.getJavaMailProperties(); p.put("mail.smtp.auth", Boolean.toString(c.auth())); p.put("mail.smtp.starttls.enable", Boolean.toString(c.security() == EmailSecurity.STARTTLS)); p.put("mail.smtp.starttls.required", Boolean.toString(c.security() == EmailSecurity.STARTTLS)); p.put("mail.smtp.ssl.enable", Boolean.toString(c.security() == EmailSecurity.SSL_TLS));
        p.put("mail.smtp.connectiontimeout", "7000"); p.put("mail.smtp.timeout", "10000"); p.put("mail.smtp.writetimeout", "10000"); return sender;
    }

    private MailConfig activeConfig() {
        var stored = repository.findAll().stream().findFirst();
        if (stored.isEmpty()) return new MailConfig(envHost, envPort, envUsername, envPassword, envFrom, envFromName, envStartTls ? EmailSecurity.STARTTLS : EmailSecurity.NONE, configuredEnv(), envAuth);
        var e = stored.get(); String password = e.getSmtpPasswordEncrypted() == null ? "" : protector.decrypt(e.getSmtpPasswordEncrypted());
        return new MailConfig(e.getSmtpHost(), e.getSmtpPort(), e.getSmtpUsername(), password, e.getFromEmail(), e.getFromName(), e.getSecurity(), e.isEnabled(), e.getSecurity() != EmailSecurity.NONE);
    }

    private boolean configuredEnv() { return envHost != null && !envHost.isBlank() && envPort > 0 && envFrom != null && !envFrom.isBlank(); }
    private EmailSettingsResponse fallbackResponse() { EmailSecurity security = envStartTls ? EmailSecurity.STARTTLS : EmailSecurity.NONE; return new EmailSettingsResponse(envHost, envPort, envUsername, envPassword != null && !envPassword.isBlank(), envFrom, envFromName, security, configuredEnv()); }
    private EmailSettingsResponse response(EmailSettingsEntity e) { return new EmailSettingsResponse(e.getSmtpHost(), e.getSmtpPort(), e.getSmtpUsername(), e.getSmtpPasswordEncrypted() != null && !e.getSmtpPasswordEncrypted().isBlank(), e.getFromEmail(), e.getFromName(), e.getSecurity(), e.isEnabled()); }
    private EmailSettingsException invalid(String message) { return new EmailSettingsException("SMTP_CONFIGURATION_INVALID", message); }
    private String trim(String value) { return value == null ? "" : value.trim(); }
    private boolean isEmail(String value) { try { new InternetAddress(value).validate(); return true; } catch (Exception e) { return false; } }
    private EmailDeliveryException deliveryException(Exception ex) {
        Throwable root = ex; while (root.getCause() != null && root.getCause() != root) root = root.getCause();
        String name = root.getClass().getName().toLowerCase(); String code = name.contains("authentication") || name.contains("auth") ? "SMTP_AUTHENTICATION_FAILED" : name.contains("ssl") || name.contains("tls") ? "SMTP_TLS_FAILED" : root instanceof ConnectException || name.contains("connect") || name.contains("timeout") ? "SMTP_CONNECTION_FAILED" : "SMTP_SEND_FAILED";
        return new EmailDeliveryException(code, "SMTP delivery failed", ex);
    }
    private record MailConfig(String host, int port, String username, String password, String fromEmail, String fromName, EmailSecurity security, boolean enabled, boolean auth) {}
}
