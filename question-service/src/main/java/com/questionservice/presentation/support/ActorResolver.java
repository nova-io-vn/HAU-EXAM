package com.questionservice.presentation.support;
import com.questionservice.domain.model.*; import java.util.UUID; import org.springframework.security.oauth2.jwt.Jwt; import org.springframework.stereotype.Component;
@Component public class ActorResolver {public Actor from(Jwt jwt){return new Actor(UUID.fromString(jwt.getSubject()),Role.valueOf(jwt.getClaimAsString("role")),jwt.getClaimAsString("facultyId"));}}
