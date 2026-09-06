package com.authservice.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableConfigurationProperties(PasswordHashingProperties.class)
public class PasswordHashingConfiguration {

    @Bean
    PasswordEncoder passwordEncoder(PasswordHashingProperties properties) {
        return new BCryptPasswordEncoder(properties.getBcryptStrength());
    }
}
