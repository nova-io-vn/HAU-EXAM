package com.eureka;

import com.netflix.eureka.EurekaServerContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
class EurekaServerApplicationTest {
    @Autowired Environment environment;
    @Autowired EurekaServerContext serverContext;

    @Test void startsEurekaRegistry() { assertThat(serverContext).isNotNull(); }

    @Test void usesRequiredStandaloneConfiguration() {
        assertThat(environment.getProperty("server.port")).isEqualTo("8761");
        assertThat(environment.getProperty("spring.application.name")).isEqualTo("eureka-server");
        assertThat(environment.getProperty("eureka.client.register-with-eureka", Boolean.class)).isFalse();
        assertThat(environment.getProperty("eureka.client.fetch-registry", Boolean.class)).isFalse();
        assertThat(environment.getProperty("eureka.client.service-url.defaultZone")).isEqualTo("http://localhost:8761/eureka/");
    }
}
