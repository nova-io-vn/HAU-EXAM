package com.gateway.presence;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnBean(PresenceService.class)
@RequestMapping("/api/v1/presence")
public class PresenceController {
    private final PresenceService presence;
    public PresenceController(PresenceService presence) { this.presence = presence; }

    @PostMapping("/heartbeat")
    public Mono<Map<String, Object>> heartbeat(@AuthenticationPrincipal Jwt jwt) {
        return presence.touch(jwt.getSubject()).thenReturn(success(Map.of("online", true)));
    }

    @GetMapping("/online-count")
    public Mono<Map<String, Object>> onlineCount(@AuthenticationPrincipal Jwt jwt) {
        if (!"SYSTEM_ADMIN".equals(jwt.getClaimAsString("role"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "SYSTEM_ADMIN role is required");
        }
        return presence.onlineCount().map(count -> success(Map.of("count", count)));
    }

    private Map<String, Object> success(Object data) {
        return Map.of("success", true, "code", "SUCCESS", "message", "Operation successful", "data", data);
    }
}
