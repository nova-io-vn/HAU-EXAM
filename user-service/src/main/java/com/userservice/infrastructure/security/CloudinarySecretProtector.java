package com.userservice.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Component
public class CloudinarySecretProtector {
    private final String configuredKey; private final SecureRandom random = new SecureRandom();
    public CloudinarySecretProtector(@Value("${CLOUDINARY_SETTINGS_ENCRYPTION_KEY:${INTERNAL_SERVICE_TOKEN:}}") String key){configuredKey=key==null?"":key.trim();}
    public String encrypt(String value){try{byte[] iv=new byte[12];random.nextBytes(iv);Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,key(),new GCMParameterSpec(128,iv));byte[] e=c.doFinal(value.getBytes(StandardCharsets.UTF_8));byte[] all=new byte[12+e.length];System.arraycopy(iv,0,all,0,12);System.arraycopy(e,0,all,12,e.length);return Base64.getEncoder().encodeToString(all);}catch(Exception e){throw new IllegalStateException("Cloudinary settings encryption is not configured",e);}}
    public String decrypt(String value){try{byte[] all=Base64.getDecoder().decode(value);Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,Arrays.copyOf(all,12)));return new String(c.doFinal(Arrays.copyOfRange(all,12,all.length)),StandardCharsets.UTF_8);}catch(Exception e){throw new IllegalStateException("Cloudinary settings decryption failed",e);}}
    private SecretKeySpec key() throws Exception {if(configuredKey.isBlank())throw new IllegalStateException("Set CLOUDINARY_SETTINGS_ENCRYPTION_KEY");return new SecretKeySpec(MessageDigest.getInstance("SHA-256").digest(configuredKey.getBytes(StandardCharsets.UTF_8)),"AES");}
}
