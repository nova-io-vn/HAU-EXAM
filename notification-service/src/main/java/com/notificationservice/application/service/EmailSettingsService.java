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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailSettingsService {
    private static final Logger log = LoggerFactory.getLogger(EmailSettingsService.class);
    private final JpaEmailSettingsRepository repository;
    private final SecretProtector protector;
    private final String envHost, envUsername, envPassword, envFrom, envFromName;
    private final int envPort;
    private final boolean envAuth, envStartTls, envStartTlsRequired, envEnabled;

    public EmailSettingsService(JpaEmailSettingsRepository repository, SecretProtector protector,
            @Value("${spring.mail.host:}") String envHost, @Value("${spring.mail.port:587}") int envPort,
            @Value("${spring.mail.username:}") String envUsername, @Value("${spring.mail.password:}") String envPassword,
            @Value("${notification.mail-from:}") String envFrom, @Value("${notification.mail-from-name:HAU QM}") String envFromName,
            @Value("${notification.mail-enabled:true}") boolean envEnabled,
            @Value("${spring.mail.properties.mail.smtp.auth:false}") boolean envAuth,
            @Value("${spring.mail.properties.mail.smtp.starttls.enable:false}") boolean envStartTls,
            @Value("${spring.mail.properties.mail.smtp.starttls.required:false}") boolean envStartTlsRequired) {
        this.repository = repository; this.protector = protector; this.envHost = envHost; this.envPort = envPort;
        this.envUsername = envUsername; this.envPassword = envPassword; this.envFrom = envFrom; this.envFromName = envFromName;
        this.envEnabled = envEnabled; this.envAuth = envAuth; this.envStartTls = envStartTls; this.envStartTlsRequired = envStartTlsRequired;
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
        if (enabled || !host.isBlank()) validateProviderConfiguration(host, port, security);
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

    @Transactional
    public boolean bootstrapFromEnvironment() {
        if (repository.count() > 0) {
            log.info("SMTP settings bootstrap skipped because database configuration already exists");
            return false;
        }
        if (!configuredEnv()) {
            log.info("SMTP settings bootstrap skipped because environment configuration is incomplete");
            return false;
        }
        EmailSettingsEntity entity = new EmailSettingsEntity();
        Instant now = Instant.now();
        entity.setId(java.util.UUID.randomUUID());
        entity.setSmtpHost(envHost.trim());
        entity.setSmtpPort(envPort);
        entity.setSmtpUsername(trim(envUsername));
        if (!trim(envPassword).isBlank()) entity.setSmtpPasswordEncrypted(protector.encrypt(envPassword.trim()));
        entity.setFromEmail(envFrom.trim());
        entity.setFromName(trim(envFromName).isBlank() ? "HAU QM" : envFromName.trim());
        entity.setSecurity(envStartTls || envStartTlsRequired ? EmailSecurity.STARTTLS : EmailSecurity.NONE);
        entity.setEnabled(envEnabled);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        repository.save(entity);
        log.info("SMTP settings bootstrapped from environment; hostConfigured=true usernameConfigured={} passwordConfigured={} security={} enabled={}",
                !trim(envUsername).isBlank(), !trim(envPassword).isBlank(), entity.getSecurity(), entity.isEnabled());
        return true;
    }

    public void sendTest(String recipient) { sendInternal(recipient, "[HAU QM] Kiểm tra cấu hình email", "Xin chào,\n\nĐây là email kiểm tra cấu hình gửi thư của hệ thống HAU QM.\n\nNếu bạn nhận được email này, cấu hình SMTP đang hoạt động bình thường.\n\nHAU QM System", false); }

    public void send(String recipient, String subject, String content) {
        sendInternal(recipient, subject, content, true);
    }

    private void sendInternal(String recipient, String subject, String content, boolean requireEnabled) {
        MailConfig config = activeConfig();
        validateProviderConfiguration(config.host(), config.port(), config.security());
        if (requireEnabled && !config.enabled()) throw new EmailDeliveryException("EMAIL_DELIVERY_DISABLED", "Gửi email hiện đang bị tắt.");
        if (config.security() != EmailSecurity.NONE && (config.username().isBlank() || config.password().isBlank()))
            throw new EmailDeliveryException("SMTP_CONFIGURATION_INVALID", "Thiếu thông tin xác thực SMTP.");
        JavaMailSenderImpl sender = createSender(config);
        try {
            var message = sender.createMimeMessage();
            var helper = new org.springframework.mail.javamail.MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(new InternetAddress(effectiveFrom(config), config.fromName(), "UTF-8")); helper.setTo(recipient); helper.setSubject(subject); helper.setText(content, false);
            sender.send(message);
            log.info("SMTP delivery completed; event=test-email recipientDomain={} smtpHost={} smtpPort={} security={}", emailDomain(recipient), config.host(), config.port(), config.security());
        } catch (Exception ex) {
            Throwable root = rootCause(ex);
            log.warn("SMTP delivery failed; event=test-email smtpHost={} smtpPort={} security={} recipientDomain={} errorType={} rootCauseType={} rootCauseMessage={}",
                    config.host(), config.port(), config.security(), emailDomain(recipient), ex.getClass().getSimpleName(), root.getClass().getSimpleName(), safeMessage(root));
            throw deliveryException(ex);
        }
    }

    private JavaMailSenderImpl createSender(MailConfig c) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl(); sender.setHost(c.host()); sender.setPort(c.port()); sender.setUsername(c.username()); sender.setPassword(c.password());
        Properties p = sender.getJavaMailProperties(); p.put("mail.smtp.auth", Boolean.toString(c.auth())); p.put("mail.smtp.starttls.enable", Boolean.toString(c.security() == EmailSecurity.STARTTLS)); p.put("mail.smtp.starttls.required", Boolean.toString(c.security() == EmailSecurity.STARTTLS)); p.put("mail.smtp.ssl.enable", Boolean.toString(c.security() == EmailSecurity.SSL_TLS));
        p.put("mail.smtp.connectiontimeout", "7000"); p.put("mail.smtp.timeout", "10000"); p.put("mail.smtp.writetimeout", "10000"); return sender;
    }

    private MailConfig activeConfig() {
        var stored = repository.findAll().stream().findFirst();
        if (stored.isEmpty()) return new MailConfig(trim(envHost), envPort, trim(envUsername), trim(envPassword), trim(envFrom), trim(envFromName), envStartTls || envStartTlsRequired ? EmailSecurity.STARTTLS : EmailSecurity.NONE, envEnabled && configuredEnv(), envAuth);
        var e = stored.get();
        String password;
        try {
            password = e.getSmtpPasswordEncrypted() == null ? "" : protector.decrypt(e.getSmtpPasswordEncrypted());
        } catch (EmailSettingsException ex) {
            log.error("SMTP credential decryption failed; errorCode={} errorType={}", ex.getCode(), ex.getClass().getSimpleName());
            throw new EmailDeliveryException("SMTP_CREDENTIAL_DECRYPTION_FAILED", "Không thể đọc thông tin xác thực SMTP đã lưu.", ex);
        }
        return new MailConfig(trim(e.getSmtpHost()), e.getSmtpPort(), trim(e.getSmtpUsername()), password, trim(e.getFromEmail()), trim(e.getFromName()), e.getSecurity(), e.isEnabled(), e.getSecurity() != EmailSecurity.NONE);
    }

    private boolean configuredEnv() { return envHost != null && !envHost.isBlank() && envPort > 0 && envFrom != null && !envFrom.isBlank() && (!envAuth || (!trim(envUsername).isBlank() && !trim(envPassword).isBlank())); }
    private EmailSettingsResponse fallbackResponse() { EmailSecurity security = envStartTls || envStartTlsRequired ? EmailSecurity.STARTTLS : EmailSecurity.NONE; return new EmailSettingsResponse(envHost, envPort, envUsername, envPassword != null && !envPassword.isBlank(), envFrom, envFromName, security, envEnabled && configuredEnv()); }
    private EmailSettingsResponse response(EmailSettingsEntity e) { return new EmailSettingsResponse(e.getSmtpHost(), e.getSmtpPort(), e.getSmtpUsername(), e.getSmtpPasswordEncrypted() != null && !e.getSmtpPasswordEncrypted().isBlank(), e.getFromEmail(), e.getFromName(), e.getSecurity(), e.isEnabled()); }
    private EmailSettingsException invalid(String message) { return new EmailSettingsException("SMTP_CONFIGURATION_INVALID", message); }
    private String trim(String value) { return value == null ? "" : value.trim(); }
    private boolean isEmail(String value) { try { new InternetAddress(value).validate(); return true; } catch (Exception e) { return false; } }
    private String emailDomain(String value) { int at = value == null ? -1 : value.lastIndexOf('@'); return at >= 0 && at + 1 < value.length() ? value.substring(at + 1) : "invalid"; }
    private String effectiveFrom(MailConfig config) {
        if (isGmail(config.host()) && isEmail(config.username())) return config.username();
        return config.fromEmail();
    }

    private void validateProviderConfiguration(String host, int port, EmailSecurity security) {
        if (host == null || host.isBlank() || port < 1 || port > 65535 || security == null)
            throw invalid("Cấu hình SMTP chưa hợp lệ.");
        if (isGmail(host) && security == EmailSecurity.STARTTLS && port != 587)
            throw invalid("Với Gmail, STARTTLS sử dụng cổng 587.");
        if (isGmail(host) && security == EmailSecurity.SSL_TLS && port != 465)
            throw invalid("Với Gmail, SSL/TLS sử dụng cổng 465.");
    }

    private boolean isGmail(String host) { return "smtp.gmail.com".equalsIgnoreCase(trim(host)); }
    private Throwable rootCause(Throwable ex) { Throwable root = ex; while (root.getCause() != null && root.getCause() != root) root = root.getCause(); return root; }
    private String safeMessage(Throwable ex) { String value = ex.getMessage(); return value == null ? "" : value.replaceAll("(?i)(password|pass|credential|authorization)\\s*[:=]\\s*[^,; ]+", "$1=[REDACTED]"); }
    private EmailDeliveryException deliveryException(Exception ex) {
        String name = ex.toString().toLowerCase() + " " + rootCause(ex).toString().toLowerCase();
        String code = name.contains("authentication") || name.contains("auth") ? "SMTP_AUTHENTICATION_FAILED" : name.contains("ssl") || name.contains("tls") || name.contains("handshake") ? "SMTP_TLS_FAILED" : ex instanceof ConnectException || name.contains("connect") || name.contains("timeout") ? "SMTP_CONNECTION_FAILED" : "EMAIL_SEND_FAILED";
        return new EmailDeliveryException(code, "SMTP delivery failed", ex);
    }
    private record MailConfig(String host, int port, String username, String password, String fromEmail, String fromName, EmailSecurity security, boolean enabled, boolean auth) {}
}
