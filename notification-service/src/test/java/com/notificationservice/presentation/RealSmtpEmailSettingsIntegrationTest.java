package com.notificationservice.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notificationservice.application.service.EmailSettingsService;
import com.notificationservice.infrastructure.mail.SecretProtector;
import com.notificationservice.infrastructure.persistence.repository.JpaEmailSettingsRepository;
import com.notificationservice.presentation.advice.GlobalExceptionHandler;
import com.notificationservice.presentation.controller.EmailSettingsController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "REAL_SMTP_TEST", matches = "true")
class RealSmtpEmailSettingsIntegrationTest {
    @Autowired
    private EmailSettingsService emailSettingsService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JpaEmailSettingsRepository repository;
    @Autowired
    private SecretProtector protector;

    @Test
    void persistsEncryptedSettingsDecryptsAndSendsOneRealEmail() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new EmailSettingsController(emailSettingsService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        String username = requiredEnvironment("REAL_SMTP_USERNAME");
        String password = requiredEnvironment("REAL_SMTP_PASSWORD");
        String recipient = requiredEnvironment("REAL_SMTP_RECIPIENT");

        Map<String, Object> settings = new LinkedHashMap<>();
        settings.put("smtpHost", "smtp.gmail.com");
        settings.put("smtpPort", 587);
        settings.put("smtpUsername", username);
        settings.put("smtpPassword", password);
        settings.put("fromEmail", username);
        settings.put("fromName", "HAU QM");
        settings.put("security", "STARTTLS");
        settings.put("enabled", true);

        mvc.perform(put("/api/v1/admin/email-settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(settings)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.smtpHost").value("smtp.gmail.com"))
                .andExpect(jsonPath("$.data.smtpPort").value(587))
                .andExpect(jsonPath("$.data.security").value("STARTTLS"))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.passwordConfigured").value(true));

        var persisted = repository.findAll().getFirst();
        assertThat(persisted.getSmtpPasswordEncrypted()).isNotBlank();
        assertThat(MessageDigest.isEqual(
                password.getBytes(StandardCharsets.UTF_8),
                persisted.getSmtpPasswordEncrypted().getBytes(StandardCharsets.UTF_8))).isFalse();
        assertThat(MessageDigest.isEqual(
                password.getBytes(StandardCharsets.UTF_8),
                protector.decrypt(persisted.getSmtpPasswordEncrypted()).getBytes(StandardCharsets.UTF_8))).isTrue();

        String getBody = mvc.perform(get("/api/v1/admin/email-settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.passwordConfigured").value(true))
                .andReturn().getResponse().getContentAsString();
        JsonNode data = objectMapper.readTree(getBody).path("data");
        assertThat(data.has("smtpPassword")).isFalse();

        mvc.perform(post("/api/v1/admin/email-settings/test")
                        .header("X-Correlation-Id", "real-smtp-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(Map.of("recipient", recipient))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));
    }

    private String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) throw new IllegalStateException(name + " is required");
        return value;
    }
}
