package com.notificationservice.application.service;

import com.notificationservice.infrastructure.mail.SecretProtector;
import com.notificationservice.infrastructure.persistence.entity.*;
import com.notificationservice.infrastructure.persistence.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.*;
import java.util.*;

@Service
public class TelegramNotificationService {
    private final JpaTelegramBotSettingsRepository bots; private final JpaTelegramConnectionRepository connections;
    private final JpaTelegramLinkTokenRepository tokens; private final JpaTelegramPreferenceRepository preferences;
    private final SecretProtector protector; private final RestClient client; private final String envToken,envUsername,apiUrl,publicUrl,webhookSecret;
    private final Map<UUID,Instant> testRateLimit=new HashMap<>(); private final Clock clock=Clock.systemUTC();
    public TelegramNotificationService(JpaTelegramBotSettingsRepository b,JpaTelegramConnectionRepository c,JpaTelegramLinkTokenRepository t,JpaTelegramPreferenceRepository p,SecretProtector s,RestClient.Builder builder,@Value("${TELEGRAM_BOT_TOKEN:}")String token,@Value("${TELEGRAM_BOT_USERNAME:}")String username,@Value("${TELEGRAM_API_URL:https://api.telegram.org}")String api,@Value("${PUBLIC_APP_URL:https://exam.nova.io.vn}")String url,@Value("${TELEGRAM_WEBHOOK_SECRET:}")String secret){bots=b;connections=c;tokens=t;preferences=p;protector=s;client=builder.build();envToken=trim(token);envUsername=trim(username).replaceFirst("^@","");apiUrl=api;publicUrl=url;webhookSecret=trim(secret);}
    @Transactional public LinkResult createLink(UUID userId){String u=currentUsername();if(u.isBlank())throw new IllegalStateException("Telegram bot username is not configured");String raw=randomToken();var e=new TelegramLinkTokenEntity();e.setTokenHash(hash(raw));e.setUserId(userId);e.setExpiresAt(now().plus(Duration.ofMinutes(10)));tokens.save(e);return new LinkResult("https://t.me/"+u+"?start="+raw,e.getExpiresAt());}
    @Transactional(readOnly=true) public Status status(UUID userId){return connections.findById(userId).map(x->new Status(true,x.getUsername(),x.getLinkedAt())).orElse(new Status(false,null,null));}
    @Transactional public void unlink(UUID userId){connections.deleteById(userId);}
    @Transactional public void savePreferences(UUID userId,PreferenceInput in){var e=preferences.findById(userId).orElseGet(()->{var x=new TelegramPreferenceEntity();x.setUserId(userId);return x;});e.setNewUsers(in.newUsers());e.setActionable(in.actionable());e.setSystemEvents(in.systemEvents());e.setLoginEvents(in.loginEvents());e.setQuestionPending(in.questionPending());e.setQuestionResubmitted(in.questionResubmitted());e.setSubjectEvents(in.subjectEvents());e.setUpdatedAt(now());preferences.save(e);}
    @Transactional(readOnly=true) public PreferenceInput preferences(UUID userId){var e=preferences.findById(userId).orElse(null);return e==null?new PreferenceInput(true,true,true,false,true,true,true):new PreferenceInput(e.isNewUsers(),e.isActionable(),e.isSystemEvents(),e.isLoginEvents(),e.isQuestionPending(),e.isQuestionResubmitted(),e.isSubjectEvents());}
    @Transactional public String handleStart(String raw,String chatId,String username){var e=tokens.findByTokenHashAndConsumedAtIsNull(hash(raw)).orElseThrow(()->new IllegalArgumentException("Telegram link is invalid or expired"));if(e.getExpiresAt().isBefore(now()))throw new IllegalArgumentException("Telegram link is expired");e.setConsumedAt(now());tokens.save(e);var c=connections.findById(e.getUserId()).orElseGet(TelegramConnectionEntity::new);c.setUserId(e.getUserId());c.setChatId(chatId);c.setUsername(username);c.setEnabled(true);c.setLinkedAt(now());connections.save(c);sendRaw(chatId,"HAU QM\n\nLiên kết Telegram thành công. Bạn sẽ nhận các thông báo đã bật tại đây.");return "OK";}
    public synchronized void test(UUID userId){Instant n=now(),old=testRateLimit.get(userId);if(old!=null&&old.plusSeconds(30).isAfter(n))throw new IllegalStateException("Telegram test rate limit exceeded");var c=connections.findById(userId).orElseThrow(()->new IllegalArgumentException("Telegram chưa được liên kết"));testRateLimit.put(userId,n);sendRaw(c.getChatId(),"HAU QM\n━━━━━━━━━━━━\n\nKết nối Telegram thành công.\nBạn sẽ nhận được các thông báo HAU QM đã bật tại đây.\n\n"+publicUrl);}
    @Transactional(readOnly=true) public AdminConfig adminConfig(){var e=bots.findAll().stream().findFirst().orElse(null);boolean configured=e!=null&&!blank(e.getBotTokenEncrypted())||!envToken.isBlank();return new AdminConfig(configured,e==null?currentUsername():e.getBotUsername(),webhookSecret.isBlank()?"NOT_CONFIGURED":"CONFIGURED",e==null?null:e.getLastCheckedAt());}
    @Transactional public AdminConfig saveConfig(String username,String token,boolean enabled){var e=bots.findAll().stream().findFirst().orElseGet(()->{var x=new TelegramBotSettingsEntity();x.setId(UUID.randomUUID());return x;});e.setBotUsername(trim(username).replaceFirst("^@",""));if(!trim(token).isBlank())e.setBotTokenEncrypted(protector.encrypt(trim(token)));e.setEnabled(enabled);e.setUpdatedAt(now());bots.save(e);return adminConfig();}
    @Transactional public void verifyBot(){String token=currentToken();if(token.isBlank())throw new IllegalStateException("Telegram bot is not configured");try{client.get().uri(apiUrl+"/bot"+token+"/getMe").retrieve().toBodilessEntity();var e=bots.findAll().stream().findFirst().orElse(null);if(e!=null){e.setLastCheckedAt(now());bots.save(e);}}catch(Exception ex){throw new IllegalStateException("Telegram bot connection failed");}}
    public void send(UUID userId,String text){connections.findById(userId).filter(TelegramConnectionEntity::isEnabled).ifPresent(x->sendRaw(x.getChatId(),text));}
    public boolean enabled(UUID userId){return connections.findById(userId).filter(TelegramConnectionEntity::isEnabled).isPresent();}
    private void sendRaw(String chatId,String text){String token=currentToken();if(token.isBlank())throw new IllegalStateException("Telegram bot is not configured");if(chatId==null||chatId.isBlank())return;client.post().uri(apiUrl+"/bot"+token+"/sendMessage").contentType(MediaType.APPLICATION_JSON).body(Map.of("chat_id",chatId,"text",text,"disable_web_page_preview",true)).retrieve().toBodilessEntity();}
    private String currentToken(){var e=bots.findAll().stream().findFirst().orElse(null);if(e!=null&&!e.isEnabled())return "";return e!=null&&!blank(e.getBotTokenEncrypted())?protector.decrypt(e.getBotTokenEncrypted()):envToken;}
    private String currentUsername(){var e=bots.findAll().stream().findFirst().orElse(null);return e!=null&&!blank(e.getBotUsername())?e.getBotUsername():envUsername;}
    private String randomToken(){byte[] b=new byte[32];new SecureRandom().nextBytes(b);return Base64.getUrlEncoder().withoutPadding().encodeToString(b);}
    private String hash(String v){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(v.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
    private Instant now(){return Instant.now(clock);} private String trim(String v){return v==null?"":v.trim();} private boolean blank(String v){return trim(v).isBlank();}
    public record LinkResult(String url,Instant expiresAt){} public record Status(boolean connected,String username,Instant linkedAt){}
    public record PreferenceInput(boolean newUsers,boolean actionable,boolean systemEvents,boolean loginEvents,boolean questionPending,boolean questionResubmitted,boolean subjectEvents){}
    public record AdminConfig(boolean configured,String botUsername,String webhookStatus,Instant lastCheckedAt){}
}
