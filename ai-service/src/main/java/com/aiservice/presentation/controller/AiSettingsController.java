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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/ai-settings")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class AiSettingsController {
    private static final Map<String, List<String>> SUPPORTED_MODELS = Map.of(
            "GEMINI", List.of("gemini-2.5-flash", "gemini-2.5-pro"),
            "OPENAI", List.of("gpt-4.1-mini", "gpt-4.1"),
            "MISTRAL", List.of("mistral-small-latest", "mistral-large-latest"));
    private final AiSettingsRepository repo;
    private final AiSecretProtector protector;
    private final RuntimeAiProviderResolver runtime;
    private final Environment environment;

    public AiSettingsController(AiSettingsRepository repo, AiSecretProtector protector, RuntimeAiProviderResolver runtime, Environment environment) {
        this.repo = repo;
        this.protector = protector;
        this.runtime = runtime;
        this.environment = environment;
    }

    public record Request(@NotBlank String provider, @NotBlank @Size(max = 160) String model, String apiKey,
                          @Size(max = 40) String persona, @Size(max = 4000) String personaInstructions) {}
    public record ProviderOption(String provider, List<String> models) {}
    public record View(String provider, String model, boolean apiKeyConfigured, String status, Instant lastCheckedAt, String lastErrorCode,
                       String persona, String personaInstructions) {
        static View from(AiSettingsEntity e, boolean environmentConfigured) {
            boolean configured = environmentConfigured || (e.apiKeyEncrypted != null && !e.apiKeyEncrypted.isBlank());
            String status = configured ? (e.runtimeStatus == null ? "CONFIGURED_BUT_UNVERIFIED" : e.runtimeStatus) : "NOT_CONFIGURED";
            return new View(e.provider, e.model, configured, status, e.lastCheckedAt, e.lastErrorCode,
                    e.persona == null ? "FRIENDLY" : e.persona, e.personaInstructions);
        }
        static View empty() { return new View("GEMINI", "gemini-2.5-flash", false, "NOT_CONFIGURED", null, null, "FRIENDLY", null); }
    }

    @GetMapping
    public ApiResponse<View> get() { return ApiResponse.ok(repo.findAll().stream().findFirst().map(e -> View.from(e, environmentCredentialConfigured(e.provider))).orElseGet(View::empty)); }

    @GetMapping("/options")
    public ApiResponse<List<ProviderOption>> options() {
        return ApiResponse.ok(SUPPORTED_MODELS.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(entry -> new ProviderOption(entry.getKey(), entry.getValue())).toList());
    }

    @PutMapping
    public ApiResponse<View> save(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody Request r) {
        String provider = r.provider().toUpperCase(Locale.ROOT);
        if (!SUPPORTED_MODELS.containsKey(provider)) throw new IllegalArgumentException("Unsupported AI provider");
        if (!SUPPORTED_MODELS.get(provider).contains(r.model().trim())) throw new IllegalArgumentException("Unsupported AI model");
        AiSettingsEntity e = repo.findAll().stream().findFirst().orElseGet(() -> { var n = new AiSettingsEntity(); n.id = UUID.randomUUID(); return n; });
        e.provider = provider;
        e.model = r.model().trim();
        String persona = r.persona() == null || r.persona().isBlank() ? "FRIENDLY" : r.persona().trim().toUpperCase(Locale.ROOT);
        if (!Set.of("FRIENDLY", "FORMAL", "CONCISE", "CUSTOM").contains(persona)) throw new IllegalArgumentException("Unsupported Kute persona");
        if ("CUSTOM".equals(persona) && (r.personaInstructions() == null || r.personaInstructions().isBlank())) throw new IllegalArgumentException("Custom persona instructions are required");
        e.persona = persona;
        e.personaInstructions = r.personaInstructions() == null || r.personaInstructions().isBlank() ? null : r.personaInstructions().trim();
        if (r.apiKey() != null && !r.apiKey().isBlank()) e.apiKeyEncrypted = protector.encrypt(r.apiKey().trim());
        e.updatedAt = Instant.now();
        e.updatedBy = UUID.fromString(jwt.getSubject());
        e.runtimeStatus = (e.apiKeyEncrypted == null || e.apiKeyEncrypted.isBlank()) && !environmentCredentialConfigured(provider) ? "NOT_CONFIGURED" : "CONFIGURED_BUT_UNVERIFIED";
        e.lastErrorCode = null;
        return ApiResponse.ok(View.from(repo.save(e), environmentCredentialConfigured(provider)));
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
        return ApiResponse.ok(View.from(repo.save(e), environmentCredentialConfigured(e.provider)));
    }

    private boolean environmentCredentialConfigured(String provider) {
        if (!"GEMINI".equalsIgnoreCase(provider)) return false;
        String key = environment.getProperty("ai.provider.api-key");
        return key != null && !key.isBlank();
    }

    private String errorCode(Exception ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) return "AI_CONNECTION_FAILED";
        String normalized = message.toLowerCase(Locale.ROOT);
        if (normalized.contains("credential")) return "AI_KEY_NOT_CONFIGURED";
        if (normalized.contains("invalid") && normalized.contains("key")) return "AI_KEY_INVALID";
        if (normalized.contains("model")) return "AI_MODEL_INVALID";
        if (normalized.contains("timeout")) return "AI_PROVIDER_TIMEOUT";
        return "AI_CONNECTION_FAILED";
    }
}
