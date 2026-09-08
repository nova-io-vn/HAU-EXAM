package com.notificationservice.infrastructure.mail;

import com.notificationservice.application.exception.EmailSettingsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class SecretProtector {
    private final String configuredKey;
    private final SecureRandom random = new SecureRandom();
    public SecretProtector(@Value("${EMAIL_SETTINGS_ENCRYPTION_KEY:}") String configuredKey) { this.configuredKey = configuredKey == null ? "" : configuredKey.trim(); }
    public String encrypt(String value) {
        try { Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding"); byte[] iv = new byte[12]; random.nextBytes(iv); cipher.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(128, iv)); byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8)); byte[] combined = new byte[iv.length + encrypted.length]; System.arraycopy(iv, 0, combined, 0, iv.length); System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length); return Base64.getEncoder().encodeToString(combined); }
        catch (EmailSettingsException e) { throw e; } catch (Exception e) { throw new EmailSettingsException("EMAIL_SETTINGS_ENCRYPTION_FAILED", "Không thể bảo vệ thông tin SMTP.", e); }
    }
    public String decrypt(String value) {
        try { byte[] combined = Base64.getDecoder().decode(value); byte[] iv = java.util.Arrays.copyOfRange(combined, 0, 12); byte[] encrypted = java.util.Arrays.copyOfRange(combined, 12, combined.length); Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(128, iv)); return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8); }
        catch (EmailSettingsException e) { throw e; } catch (Exception e) { throw new EmailSettingsException("EMAIL_SETTINGS_DECRYPTION_FAILED", "Không thể đọc thông tin SMTP đã lưu.", e); }
    }
    private SecretKeySpec key() {
        if (configuredKey.isBlank()) throw new EmailSettingsException("EMAIL_SETTINGS_ENCRYPTION_NOT_CONFIGURED", "Máy chủ chưa được cấu hình khóa bảo vệ thông tin SMTP.");
        byte[] bytes; try { bytes = Base64.getDecoder().decode(configuredKey); } catch (IllegalArgumentException e) { bytes = configuredKey.getBytes(StandardCharsets.UTF_8); }
        if (bytes.length != 16 && bytes.length != 24 && bytes.length != 32) throw new EmailSettingsException("EMAIL_SETTINGS_ENCRYPTION_NOT_CONFIGURED", "Khóa bảo vệ thông tin SMTP không hợp lệ.");
        return new SecretKeySpec(bytes, "AES");
    }
}
