package com.notificationservice.application.service;

import com.notificationservice.infrastructure.mail.SecretProtector;
import com.notificationservice.infrastructure.persistence.entity.TelegramBotSettingsEntity;
import com.notificationservice.infrastructure.persistence.entity.TelegramConnectionEntity;
import com.notificationservice.infrastructure.persistence.entity.TelegramLinkTokenEntity;
import com.notificationservice.infrastructure.persistence.repository.JpaTelegramBotSettingsRepository;
import com.notificationservice.infrastructure.persistence.repository.JpaTelegramConnectionRepository;
import com.notificationservice.infrastructure.persistence.repository.JpaTelegramLinkTokenRepository;
import com.notificationservice.infrastructure.persistence.repository.JpaTelegramPreferenceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class TelegramNotificationService {
    private final JpaTelegramBotSettingsRepository bots;
    private final JpaTelegramConnectionRepository connections;
    private final JpaTelegramLinkTokenRepository tokens;
    private final JpaTelegramPreferenceRepository preferences;
    private final SecretProtector protector;
    private final RestClient client;
    private final String envToken, envUsername, apiUrl, publicUrl;
    private final Map<UUID, Instant> testRateLimit = new HashMap<>();
    private final Clock clock = new Clock();

    public TelegramNotificationService(JpaTelegramBotSettingsRepository bots, JpaTelegramConnectionRepository connections,
                                       JpaTelegramLinkTokenRepository tokens, JpaTelegramPreferenceRepository preferences,
                                       SecretProtector protector, RestClient.Builder builder,
                                       @Value("${TELEGRAM_BOT_TOKEN:}") String token,
                                       @Value("${TELEGRAM_BOT_USERNAME:}") String username,
                                       @Value("${TELEGRAM_API_URL:https://api.telegram.org}") String apiUrl,
                                       @Value("${PUBLIC_APP_URL:https://exam.nova.io.vn}") String publicUrl) {
        this.bots = bots; this.connections = connections; this.tokens = tokens; this.preferences = preferences;
        this.protector = protector; this.client = builder.build(); this.envToken = trim(token);
        this.envUsername = trim(username).replaceFirst("^@", ""); this.apiUrl = apiUrl; this.publicUrl = publicUrl;
    }

    @Transactional
    public LinkResult createLink(UUID userId) {
        String username = currentUsername();
        if (username.isBlank()) throw new IllegalStateException("Telegram bot username is not configured");
        String raw = randomToken();
        TelegramLinkTokenEntity entity = new TelegramLinkTokenEntity();
        entity.setTokenHash(hash(raw)); entity.setUserId(userId); entity.setExpiresAt(clock.now().plus(Duration.ofMinutes(10)));
        tokens.save(entity);
        return new LinkResult("https://t.me/" + username + "?start=" + raw, entity.getExpiresAt());
    }

    @Transactional(readOnly = true)
    public Status status(UUID userId) { return connections.findById(userId).map(x -> new Status(true, x.getUsername(), x.getLinkedAt())).orElse(new Status(false, null, null)); }

    @Transactional public void unlink(UUID userId) { connections.deleteById(userId); }

    @Transactional
    public void savePreferences(UUID userId, PreferenceInput input) {
        var entity = preferences.findById(userId).orElseGet(() -> { var value = new com.notificationservice.infrastructure.persistence.entity.TelegramPreferenceEntity(); value.setUserId(userId); return value; });
        entity.setNewUsers(input.newUsers()); entity.setActionable(input.actionable()); entity.setSystemEvents(input.systemEvents()); entity.setLoginEvents(input.loginEvents());
        entity.setQuestionPending(input.questionPending()); entity.setQuestionResubmitted(input.questionResubmitted()); entity.setSubjectEvents(input.subjectEvents()); entity.setUpdatedAt(clock.now());
        preferences.save(entity);
    }

    @Transactional(readOnly = true)
    public PreferenceInput preferences(UUID userId) {
        var e = preferences.findById(userId).orElse(null);
        return e == null ? new PreferenceInput(true, true, true, false, true, true, true) : new PreferenceInput(e.isNewUsers(), e.isActionable(), e.isSystemEvents(), e.isLoginEvents(), e.isQuestionPending(), e.isQuestionResubmitted(), e.isSubjectEvents());
    }

    @Transactional
    public String handleStart(String rawToken, String chatId, String username) {
        var entity = tokens.findByTokenHashAndConsumedAtIsNull(hash(rawToken)).orElseThrow(() -> new IllegalArgumentException("Telegram link is invalid or expired"));
        if (entity.getExpiresAt().isBefore(clock.now())) throw new IllegalArgumentException("Telegram link is expired");
        entity.setConsumedAt(clock.now()); tokens.save(entity);
        var connection = connections.findById(entity.getUserId()).orElseGet(TelegramConnectionEntity::new);
        connection.setUserId(entity.getUserId()); connection.setChatId(chatId); connection.setUsername(username); connection.setEnabled(true); connection.setLinkedAt(clock.now()); connections.save(connection);
        sendRaw(chatId, "✅ Liên kết HAU QM thành công. Bạn sẽ nhận các thông báo đã bật tại đây.");
        return "✅ Liên kết HAU QM thành công.";
    }

    public synchronized void test(UUID userId) {
        Instant now = clock.now(); Instant previous = testRateLimit.get(userId);
        if (previous != null && previous.plusSeconds(30).isAfter(now)) throw new IllegalStateException("Telegram test rate limit exceeded");
        var connection = connections.findById(userId).orElseThrow(() -> new IllegalArgumentException("Telegram chưa được liên kết"));
        testRateLimit.put(userId, now);
        sendRaw(connection.getChatId(), "HAU QM\n\nKết nối Telegram của bạn đang hoạt động bình thường.\nBạn sẽ nhận các thông báo đã bật từ hệ thống tại đây.\n\n" + publicUrl);
    }

    @Transactional(readOnly = true)
    public AdminConfig adminConfig() { var e = bots.findAll().stream().findFirst().orElse(null); return new AdminConfig(e == null ? currentUsername() : e.getBotUsername(), e != null && e.isEnabled(), e != null && e.getBotTokenEncrypted() != null && !e.getBotTokenEncrypted().isBlank() || !envToken.isBlank()); }

    @Transactional
    public AdminConfig saveConfig(String username, String token, boolean enabled) {
        var e = bots.findAll().stream().findFirst().orElseGet(() -> { var value = new TelegramBotSettingsEntity(); value.setId(UUID.randomUUID()); return value; });
        e.setBotUsername(trim(username).replaceFirst("^@", "")); if (!trim(token).isBlank()) e.setBotTokenEncrypted(protector.encrypt(trim(token))); e.setEnabled(enabled); e.setUpdatedAt(clock.now()); bots.save(e); return adminConfig();
    }

    public void verifyBot() { String token = currentToken(); if (token.isBlank()) throw new IllegalStateException("Telegram bot is not configured"); try { client.get().uri(apiUrl + "/bot" + token + "/getMe").retrieve().toBodilessEntity(); } catch (Exception e) { throw new IllegalStateException("Telegram bot connection failed", e); } }
    public void send(UUID userId, String text) { connections.findById(userId).filter(TelegramConnectionEntity::isEnabled).ifPresent(x -> sendRaw(x.getChatId(), text)); }
    public boolean enabled(UUID userId) { return connections.findById(userId).filter(TelegramConnectionEntity::isEnabled).isPresent(); }

    private void sendRaw(String chatId, String text) { String token = currentToken(); if (token.isBlank()) throw new IllegalStateException("Telegram bot is not configured"); if (chatId == null || chatId.isBlank()) return; try { client.post().uri(apiUrl + "/bot" + token + "/sendMessage").contentType(MediaType.APPLICATION_JSON).body(Map.of("chat_id", chatId, "text", text, "disable_web_page_preview", true)).retrieve().toBodilessEntity(); } catch (Exception e) { throw new IllegalStateException("Telegram delivery failed", e); } }
    private String currentToken() { var e = bots.findAll().stream().findFirst().orElse(null); if (e != null && !e.isEnabled()) return ""; return e != null && e.getBotTokenEncrypted() != null && !e.getBotTokenEncrypted().isBlank() ? protector.decrypt(e.getBotTokenEncrypted()) : envToken; }
    private String currentUsername() { var e = bots.findAll().stream().findFirst().orElse(null); return e != null && e.getBotUsername() != null && !e.getBotUsername().isBlank() ? e.getBotUsername() : envUsername; }
    private String randomToken() { byte[] bytes = new byte[32]; new SecureRandom().nextBytes(bytes); return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); }
    private String hash(String value) { try { return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); } catch (Exception e) { throw new IllegalStateException(e); } }
    private String trim(String value) { return value == null ? "" : value.trim(); }
    private record Clock() { Instant now() { return Instant.now(); } }
    public record LinkResult(String url, Instant expiresAt) {}
    public record Status(boolean connected, String username, Instant linkedAt) {}
    public record PreferenceInput(boolean newUsers, boolean actionable, boolean systemEvents, boolean loginEvents, boolean questionPending, boolean questionResubmitted, boolean subjectEvents) {}
    public record AdminConfig(String botUsername, boolean enabled, boolean tokenConfigured) {}
}
