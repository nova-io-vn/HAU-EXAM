package com.notificationservice.application.service;

import com.notificationservice.application.exception.EmailDeliveryException;
import com.notificationservice.application.exception.EmailSettingsException;
import com.notificationservice.domain.model.EmailSecurity;
import com.notificationservice.infrastructure.mail.SecretProtector;
import com.notificationservice.infrastructure.persistence.entity.EmailSettingsEntity;
import com.notificationservice.infrastructure.persistence.repository.JpaEmailSettingsRepository;
import com.notificationservice.presentation.request.SmtpSettingsRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;

import javax.net.ssl.SSLHandshakeException;
import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailSettingsServiceTest {
    @Mock
    private JpaEmailSettingsRepository repository;

    private SecretProtector protector;
    private EmailSettingsService service;

    @BeforeEach
    void setUp() {
        protector = new SecretProtector("0123456789abcdef0123456789abcdef");
        service = new EmailSettingsService(repository, protector, "", 587, "", "", "", "HAU QM",
                false, false, false, false);
    }

    @Test
    void mapsStartTlsToExplicitTlsWithoutImplicitSsl() {
        var sender = service.createSender(new EmailSettingsService.MailConfig(
                "smtp.gmail.com", 587, "sender@example.com", "secret", "sender@example.com", "HAU QM",
                EmailSecurity.STARTTLS, true, true));

        assertThat(sender.getPort()).isEqualTo(587);
        assertThat(sender.getJavaMailProperties())
                .containsEntry("mail.smtp.auth", "true")
                .containsEntry("mail.smtp.starttls.enable", "true")
                .containsEntry("mail.smtp.starttls.required", "true")
                .containsEntry("mail.smtp.ssl.enable", "false");
    }

    @Test
    void mapsSslTlsToImplicitSslWithoutStartTls() {
        var sender = service.createSender(new EmailSettingsService.MailConfig(
                "smtp.gmail.com", 465, "sender@example.com", "secret", "sender@example.com", "HAU QM",
                EmailSecurity.SSL_TLS, true, true));

        assertThat(sender.getPort()).isEqualTo(465);
        assertThat(sender.getJavaMailProperties())
                .containsEntry("mail.smtp.auth", "true")
                .containsEntry("mail.smtp.starttls.enable", "false")
                .containsEntry("mail.smtp.starttls.required", "false")
                .containsEntry("mail.smtp.ssl.enable", "true");
    }

    @Test
    void rejectsGmailPort587WithImplicitSslBeforeSending() {
        assertThatThrownBy(() -> service.validateProviderConfiguration("smtp.gmail.com", 587, EmailSecurity.SSL_TLS))
                .isInstanceOf(EmailSettingsException.class)
                .extracting(error -> ((EmailSettingsException) error).getCode(), Throwable::getMessage)
                .containsExactly("SMTP_CONFIGURATION_INVALID",
                        "Gmail cổng 587 sử dụng STARTTLS. SSL/TLS sử dụng cổng 465.");
    }

    @Test
    void blankOrUiPlaceholderKeepsExistingEncryptedPassword() {
        EmailSettingsEntity entity = storedSettings();
        String encryptedPassword = protector.encrypt("existing-password");
        entity.setSmtpPasswordEncrypted(encryptedPassword);
        when(repository.findAll()).thenReturn(List.of(entity));
        when(repository.save(any(EmailSettingsEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.update(request(null));
        assertThat(entity.getSmtpPasswordEncrypted()).isEqualTo(encryptedPassword);

        service.update(request("••••••••"));
        assertThat(entity.getSmtpPasswordEncrypted()).isEqualTo(encryptedPassword);
    }

    @Test
    void responseOnlyReportsPasswordConfiguredAndNeverReturnsPassword() {
        EmailSettingsEntity entity = storedSettings();
        entity.setSmtpPasswordEncrypted(protector.encrypt("existing-password"));
        when(repository.findAll()).thenReturn(List.of(entity));

        var response = service.get();

        assertThat(response.passwordConfigured()).isTrue();
        assertThat(response.toString()).doesNotContain("existing-password", entity.getSmtpPasswordEncrypted());
    }

    @Test
    void classifiesAuthenticationConnectionTlsAndOtherMailFailures() {
        assertThat(service.deliveryException(new MailAuthenticationException("535 rejected")).getCode())
                .isEqualTo("SMTP_AUTHENTICATION_FAILED");
        assertThat(service.deliveryException(new MailSendException("timeout", new SocketTimeoutException())).getCode())
                .isEqualTo("SMTP_CONNECTION_FAILED");
        assertThat(service.deliveryException(new MailSendException("tls", new SSLHandshakeException("failed"))).getCode())
                .isEqualTo("SMTP_TLS_FAILED");
        assertThat(service.deliveryException(new MailSendException("other failure")).getCode())
                .isEqualTo("EMAIL_SEND_FAILED");
    }

    @Test
    void reportsCredentialDecryptionFailureWithoutAttemptingSmtp() {
        EmailSettingsEntity entity = storedSettings();
        SecretProtector differentKey = new SecretProtector("abcdef0123456789abcdef0123456789");
        entity.setSmtpPasswordEncrypted(differentKey.encrypt("existing-password"));
        when(repository.findAll()).thenReturn(List.of(entity));

        assertThatThrownBy(() -> service.sendTest("recipient@example.com"))
                .isInstanceOf(EmailDeliveryException.class)
                .extracting(error -> ((EmailDeliveryException) error).getCode())
                .isEqualTo("SMTP_CREDENTIAL_DECRYPTION_FAILED");
    }

    @Test
    void testEmailHonorsDisabledPersistedSetting() {
        EmailSettingsEntity entity = storedSettings();
        entity.setEnabled(false);
        entity.setSmtpPasswordEncrypted(protector.encrypt("existing-password"));
        when(repository.findAll()).thenReturn(List.of(entity));

        assertThatThrownBy(() -> service.sendTest("recipient@example.com"))
                .isInstanceOf(EmailDeliveryException.class)
                .extracting(error -> ((EmailDeliveryException) error).getCode())
                .isEqualTo("EMAIL_DELIVERY_DISABLED");
    }

    private SmtpSettingsRequest request(String password) {
        return new SmtpSettingsRequest("smtp.gmail.com", 587, "sender@example.com", password,
                "sender@example.com", "HAU QM", EmailSecurity.STARTTLS, true);
    }

    private EmailSettingsEntity storedSettings() {
        EmailSettingsEntity entity = new EmailSettingsEntity();
        entity.setId(UUID.randomUUID());
        entity.setSmtpHost("smtp.gmail.com");
        entity.setSmtpPort(587);
        entity.setSmtpUsername("sender@example.com");
        entity.setFromEmail("sender@example.com");
        entity.setFromName("HAU QM");
        entity.setSecurity(EmailSecurity.STARTTLS);
        entity.setEnabled(true);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }
}
