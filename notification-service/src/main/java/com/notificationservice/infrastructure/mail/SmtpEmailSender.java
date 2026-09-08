package com.notificationservice.infrastructure.mail;

import com.notificationservice.application.exception.EmailDeliveryException;
import com.notificationservice.application.port.out.EmailSender;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class SmtpEmailSender implements EmailSender {
    private static final Logger log = LoggerFactory.getLogger(SmtpEmailSender.class);
    private final JavaMailSender sender;
    private final String from;

    public SmtpEmailSender(JavaMailSender sender, @Value("${notification.mail-from}") String from) {
        this.sender = sender;
        this.from = from;
    }

    @Override
    public void send(String recipient, String subject, String content) {
        try {
            var message = sender.createMimeMessage();
            var helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(from);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(content, false);
            sender.send(message);
        } catch (MessagingException | MailException ex) {
            Throwable root = ex;
            while (root.getCause() != null && root.getCause() != root) {
                root = root.getCause();
            }
            log.warn("SMTP delivery failed; type={}, rootType={}", ex.getClass().getSimpleName(), root.getClass().getSimpleName());
            throw new EmailDeliveryException("Email delivery failed", ex);
        }
    }
}
