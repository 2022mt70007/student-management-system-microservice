package com.sms.common.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Converter
public class SensitiveStringEncryptor implements AttributeConverter<String, String> {

    private static final String AES = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final byte[] KEY = buildKey();

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isBlank()) {
            return attribute;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(KEY, AES), new GCMParameterSpec(TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            byte[] packed = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, packed, 0, iv.length);
            System.arraycopy(encrypted, 0, packed, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(packed);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to encrypt sensitive data", ex);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return dbData;
        }
        try {
            byte[] packed = Base64.getDecoder().decode(dbData);
            if (packed.length <= IV_LENGTH) {
                return dbData;
            }
            byte[] iv = new byte[IV_LENGTH];
            byte[] encrypted = new byte[packed.length - IV_LENGTH];
            System.arraycopy(packed, 0, iv, 0, IV_LENGTH);
            System.arraycopy(packed, IV_LENGTH, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(KEY, AES), new GCMParameterSpec(TAG_LENGTH, iv));
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            // Backward compatibility for legacy plaintext rows before encryption rollout.
            return dbData;
        }
    }

    private static byte[] buildKey() {
        String source = System.getenv().getOrDefault("APP_ENCRYPTION_KEY",
                "CHANGE_ME_IN_PROD_PHASE3_ENCRYPTION_KEY");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(source.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to initialize encryption key", ex);
        }
    }
}
