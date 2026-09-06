package com.aiservice.infrastructure.config;
import com.aiservice.infrastructure.persistence.mapper.DocumentMapper;
import java.time.Clock;
import org.springframework.context.annotation.*;
import org.springframework.web.client.RestClient;
@Configuration public class ApplicationConfig {
 @Bean Clock clock(){return Clock.systemUTC();}
 @Bean RestClient.Builder restClientBuilder(){return RestClient.builder();}
 @Bean DocumentMapper documentMapper(){return new DocumentMapper();}
}
