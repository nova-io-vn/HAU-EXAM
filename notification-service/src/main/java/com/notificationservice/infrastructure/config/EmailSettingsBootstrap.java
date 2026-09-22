package com.notificationservice.infrastructure.config;

import com.notificationservice.application.service.EmailSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class EmailSettingsBootstrap implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(EmailSettingsBootstrap.class);
    private final EmailSettingsService settings;

    public EmailSettingsBootstrap(EmailSettingsService settings) { this.settings = settings; }

    @Override
    public void run(ApplicationArguments args) {
        try { settings.bootstrapFromEnvironment(); }
        catch (RuntimeException exception) {
            log.error("SMTP settings bootstrap failed; existing database configuration remains unchanged; errorType={}", exception.getClass().getSimpleName());
        }
    }
}
