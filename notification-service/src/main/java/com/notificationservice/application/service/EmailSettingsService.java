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
import jakarta.mail.AuthenticationFailedException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.internet.InternetAddress;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import javax.net.ssl.SSLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@Service
public class EmailSettingsService {
    private static final Logger log = LoggerFactory.getLogger(EmailSettingsService.class);
    private static final List<String> PASSWORD_PLACEHOLDERS = List.of("••••••••", "••••••••••••");
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
        String submittedPassword = normalizedPassword(request.smtpPassword());
        if (enabled && !hasOldPassword && submittedPassword.isBlank() && security != EmailSecurity.NONE)
            throw invalid("Mật khẩu SMTP là bắt buộc khi bật gửi email lần đầu.");
        entity.setSmtpHost(host); entity.setSmtpPort(port); entity.setSmtpUsername(username); entity.setFromEmail(from);
        entity.setFromName(fromName.isBlank() ? "HAU QM" : fromName); entity.setSecurity(security); entity.setEnabled(enabled); entity.setUpdatedAt(Instant.now());
        if (!submittedPassword.isBlank()) entity.setSmtpPasswordEncrypted(protector.encrypt(submittedPassword));
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

    public void sendTest(String recipient) { sendInternal(recipient, "[HAU QM] Kiểm tra cấu hình email", "Xin chào,\n\nĐây là email kiểm tra cấu hình gửi thư của hệ thống HAU QM.\n\nNếu bạn nhận được email này, cấu hình SMTP đang hoạt động bình thường.\n\nHAU QM System", "test-email"); }

    public void send(String recipient, String subject, String content) {
        sendInternal(recipient, subject, content, "notification-email");
    }

    private void sendInternal(String recipient, String subject, String content, String operation) {
        MailConfig config = activeConfig();
        validateProviderConfiguration(config.host(), config.port(), config.security());
        if (!config.enabled()) throw new EmailDeliveryException("EMAIL_DELIVERY_DISABLED", "Gửi email hiện đang bị tắt.");
        if (config.security() != EmailSecurity.NONE && (config.username().isBlank() || config.password().isBlank()))
            throw new EmailDeliveryException("SMTP_CONFIGURATION_INVALID", "Thiếu thông tin xác thực SMTP.");
        JavaMailSenderImpl sender = createSender(config);
        try {
            var message = sender.createMimeMessage();
            var helper = new org.springframework.mail.javamail.MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(new InternetAddress(effectiveFrom(config), config.fromName(), "UTF-8")); helper.setTo(recipient); helper.setSubject(subject); helper.setText(content, false);
            sender.send(message);
            log.info("SMTP delivery completed; operation={} result=SMTP_ACCEPTED recipientDomain={} smtpHost={} smtpPort={} security={} correlationId={}",
                    operation, emailDomain(recipient), config.host(), config.port(), config.security(), correlationId());
        } catch (Exception ex) {
            Throwable root = rootCause(ex);
            EmailDeliveryException classified = deliveryException(ex);
            log.warn("SMTP delivery failed; operation={} smtpHost={} smtpPort={} security={} recipientDomain={} correlationId={} exception={} rootException={} result={}",
                    operation, config.host(), config.port(), config.security(), emailDomain(recipient), correlationId(),
                    ex.getClass().getSimpleName(), root.getClass().getSimpleName(), classified.getCode());
            throw classified;
        }
    }

    JavaMailSenderImpl createSender(MailConfig c) {
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

    void validateProviderConfiguration(String host, int port, EmailSecurity security) {
        if (host == null || host.isBlank() || port < 1 || port > 65535 || security == null)
            throw invalid("Cấu hình SMTP chưa hợp lệ.");
        if (isGmail(host) && port == 587 && security != EmailSecurity.STARTTLS)
            throw invalid("Gmail cổng 587 sử dụng STARTTLS. SSL/TLS sử dụng cổng 465.");
        if (isGmail(host) && port == 465 && security != EmailSecurity.SSL_TLS)
            throw invalid("Gmail cổng 465 sử dụng SSL/TLS. STARTTLS sử dụng cổng 587.");
        if (isGmail(host) && security == EmailSecurity.STARTTLS && port != 587)
            throw invalid("Gmail STARTTLS sử dụng cổng 587.");
        if (isGmail(host) && security == EmailSecurity.SSL_TLS && port != 465)
            throw invalid("Gmail SSL/TLS sử dụng cổng 465.");
    }

    private boolean isGmail(String host) { return "smtp.gmail.com".equalsIgnoreCase(trim(host)); }
    private Throwable rootCause(Throwable ex) { Throwable root = ex; while (root.getCause() != null && root.getCause() != root) root = root.getCause(); return root; }
    private String normalizedPassword(String value) {
        String normalized = trim(value);
        return PASSWORD_PLACEHOLDERS.contains(normalized) ? "" : normalized;
    }
    private String correlationId() { String value = MDC.get("correlationId"); return value == null || value.isBlank() ? "-" : value; }
    EmailDeliveryException deliveryException(Exception ex) {
        String code;
        if (hasCause(ex, MailAuthenticationException.class) || hasCause(ex, AuthenticationFailedException.class) || containsAny(ex, "535", "authentication failed", "authentication unsuccessful")) {
            code = "SMTP_AUTHENTICATION_FAILED";
        } else if (hasCause(ex, SSLException.class) || containsAny(ex, "starttls", "could not convert socket to tls", "handshake_failure")) {
            code = "SMTP_TLS_FAILED";
        } else if (hasCause(ex, ConnectException.class) || hasCause(ex, SocketTimeoutException.class) || containsAny(ex, "couldn't connect", "could not connect", "connection timed out")) {
            code = "SMTP_CONNECTION_FAILED";
        } else {
            code = "EMAIL_SEND_FAILED";
        }
        return new EmailDeliveryException(code, "SMTP delivery failed", ex);
    }
    private boolean hasCause(Throwable error, Class<? extends Throwable> type) {
        for (Throwable current = error; current != null; current = current.getCause()) if (type.isInstance(current)) return true;
        if (error instanceof MailSendException sendException)
            for (Throwable failure : sendException.getMessageExceptions()) if (hasCause(failure, type)) return true;
        return false;
    }
    private boolean containsAny(Throwable error, String... needles) {
        for (Throwable current = error; current != null; current = current.getCause()) {
            String text = (current.getClass().getName() + " " + current.getMessage()).toLowerCase();
            for (String needle : needles) if (text.contains(needle)) return true;
        }
        return false;
    }
    record MailConfig(String host, int port, String username, String password, String fromEmail, String fromName, EmailSecurity security, boolean enabled, boolean auth) {}
}
