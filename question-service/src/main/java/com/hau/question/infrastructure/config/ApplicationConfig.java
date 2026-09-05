package com.hau.question.infrastructure.config;
import java.time.Clock; import org.springframework.context.annotation.*;
@Configuration public class ApplicationConfig { @Bean Clock clock(){return Clock.systemUTC();} }
