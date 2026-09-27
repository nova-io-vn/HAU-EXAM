package com.notificationservice.presentation.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.notificationservice.application.service.TelegramNotificationService;
import com.notificationservice.presentation.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

@RestController
public class TelegramController {
    private final TelegramNotificationService service;
    private final String webhookSecret;
    public TelegramController(TelegramNotificationService service, @Value("${TELEGRAM_WEBHOOK_SECRET:}") String webhookSecret) { this.service = service; this.webhookSecret = webhookSecret == null ? "" : webhookSecret; }
    @GetMapping("/api/v1/telegram/status") public ApiResponse<?> status(@AuthenticationPrincipal Jwt jwt) { return ApiResponse.success(service.status(user(jwt))); }
    @PostMapping("/api/v1/telegram/link") public ApiResponse<?> link(@AuthenticationPrincipal Jwt jwt) { return ApiResponse.success(service.createLink(user(jwt))); }
    @PostMapping("/api/v1/telegram/test") public ApiResponse<Void> test(@AuthenticationPrincipal Jwt jwt) { service.test(user(jwt)); return ApiResponse.success(null); }
    @PostMapping("/api/v1/telegram/unlink") public ApiResponse<Void> unlink(@AuthenticationPrincipal Jwt jwt) { service.unlink(user(jwt)); return ApiResponse.success(null); }
    @GetMapping("/api/v1/telegram/preferences") public ApiResponse<?> preferences(@AuthenticationPrincipal Jwt jwt) { return ApiResponse.success(service.preferences(user(jwt))); }
    @PutMapping("/api/v1/telegram/preferences") public ApiResponse<Void> preferences(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TelegramNotificationService.PreferenceInput input) { service.savePreferences(user(jwt), input); return ApiResponse.success(null); }
    @PreAuthorize("hasRole('SYSTEM_ADMIN')") @GetMapping("/api/v1/admin/telegram") public ApiResponse<?> admin() { return ApiResponse.success(service.adminConfig()); }
    @PreAuthorize("hasRole('SYSTEM_ADMIN')") @PutMapping("/api/v1/admin/telegram") public ApiResponse<?> save(@Valid @RequestBody BotRequest request) { return ApiResponse.success(service.saveConfig(request.botUsername(), request.botToken(), request.enabled())); }
    @PreAuthorize("hasRole('SYSTEM_ADMIN')") @PostMapping("/api/v1/admin/telegram/test") public ApiResponse<?> testConfig() { return ApiResponse.success(service.verifyBot()); }
    @PostMapping({"/api/v1/integrations/telegram/webhook", "/api/v1/public/telegram/webhook"})
    public ApiResponse<String> webhook(@RequestHeader(value = "X-Telegram-Bot-Api-Secret-Token", required = false) String secret, @RequestBody JsonNode update) {
        if (webhookSecret.isBlank() || secret == null || !MessageDigest.isEqual(secret.getBytes(StandardCharsets.UTF_8), webhookSecret.getBytes(StandardCharsets.UTF_8))) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        JsonNode message = update.path("message"), chat = message.path("chat"), from = message.path("from");
        String text = message.path("text").asText("");
        if (text.startsWith("/start ")) service.handleStart(text.substring(7).trim(), chat.path("id").asText(), from.path("username").asText(null));
        return ApiResponse.success("OK");
    }
    private UUID user(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
    public record BotRequest(@NotBlank String botUsername, String botToken, boolean enabled) { }
}
