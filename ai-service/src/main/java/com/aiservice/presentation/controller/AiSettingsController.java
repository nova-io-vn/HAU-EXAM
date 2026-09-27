package com.aiservice.presentation.controller;

import com.aiservice.infrastructure.external.RuntimeAiProviderResolver;
import com.aiservice.infrastructure.persistence.entity.AiSettingsEntity;
import com.aiservice.infrastructure.persistence.repository.AiSettingsRepository;
import com.aiservice.infrastructure.security.AiSecretProtector;
import com.aiservice.presentation.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/ai-settings")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class AiSettingsController {
    private final AiSettingsRepository repo;
    private final AiSecretProtector protector;
    private final RuntimeAiProviderResolver runtime;

    public AiSettingsController(AiSettingsRepository repo, AiSecretProtector protector, RuntimeAiProviderResolver runtime) {
        this.repo = repo;
        this.protector = protector;
        this.runtime = runtime;
    }

    public record Request(@NotBlank String provider, @NotBlank @Size(max = 160) String model, String apiKey) {}
    public record View(String provider, String model, boolean apiKeyConfigured, String status, Instant lastCheckedAt, String lastErrorCode) {
        static View from(AiSettingsEntity e) {
            boolean configured = e.apiKeyEncrypted != null && !e.apiKeyEncrypted.isBlank();
            String status = configured ? (e.runtimeStatus == null ? "CONFIGURED_BUT_UNVERIFIED" : e.runtimeStatus) : "NOT_CONFIGURED";
            return new View(e.provider, e.model, configured, status, e.lastCheckedAt, e.lastErrorCode);
        }
        static View empty() { return new View("GEMINI", "gemini-2.5-flash", false, "NOT_CONFIGURED", null, null); }
    }

    @GetMapping
    public ApiResponse<View> get() { return ApiResponse.ok(repo.findAll().stream().findFirst().map(View::from).orElseGet(View::empty)); }

    @PutMapping
    public ApiResponse<View> save(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody Request r) {
        String provider = r.provider().toUpperCase(Locale.ROOT);
        if (!Set.of("OPENAI", "GEMINI", "MISTRAL").contains(provider)) throw new IllegalArgumentException("Unsupported AI provider");
        AiSettingsEntity e = repo.findAll().stream().findFirst().orElseGet(() -> { var n = new AiSettingsEntity(); n.id = UUID.randomUUID(); return n; });
        e.provider = provider;
        e.model = r.model().trim();
        if (r.apiKey() != null && !r.apiKey().isBlank()) e.apiKeyEncrypted = protector.encrypt(r.apiKey().trim());
        e.updatedAt = Instant.now();
        e.updatedBy = UUID.fromString(jwt.getSubject());
        e.runtimeStatus = e.apiKeyEncrypted == null || e.apiKeyEncrypted.isBlank() ? "NOT_CONFIGURED" : "CONFIGURED_BUT_UNVERIFIED";
        e.lastErrorCode = null;
        return ApiResponse.ok(View.from(repo.save(e)));
    }

    @PostMapping("/test")
    public ApiResponse<View> test() {
        AiSettingsEntity e = repo.findAll().stream().findFirst().orElseThrow(() -> new IllegalStateException("AI_NOT_CONFIGURED"));
        e.lastCheckedAt = Instant.now();
        try {
            runtime.testConnection();
            e.runtimeStatus = "WORKING";
            e.lastErrorCode = null;
        } catch (Exception ex) {
            e.runtimeStatus = "ERROR";
            e.lastErrorCode = errorCode(ex);
        }
        return ApiResponse.ok(View.from(repo.save(e)));
    }

    private String errorCode(Exception ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) return "AI_CONNECTION_FAILED";
        String normalized = message.toLowerCase(Locale.ROOT);
        if (normalized.contains("credential")) return "AI_KEY_NOT_CONFIGURED";
        if (normalized.contains("invalid") && normalized.contains("key")) return "AI_KEY_INVALID";
        if (normalized.contains("timeout")) return "AI_PROVIDER_TIMEOUT";
        return "AI_CONNECTION_FAILED";
    }
}
