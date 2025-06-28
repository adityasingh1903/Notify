package com.notify.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EncryptionUtil {

    private static final String SECRET_KEY = "MySecretKey12345"; // 16-char key
    private static final String ALGORITHM = "AES";

    public static String encrypt(String input) throws Exception {
        SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String encryptedInput) throws Exception {
        if (encryptedInput == null || encryptedInput.trim().isEmpty()) {
            return "";
        }

        String trimmedInput = encryptedInput.trim();

        // Optional base64 format check (prevents runtime crash)
        if (!trimmedInput.matches("^[A-Za-z0-9+/=\\r\\n]+$")) {
            throw new IllegalArgumentException("Invalid Base64 input: " + trimmedInput);
        }

        SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decoded = Base64.getDecoder().decode(trimmedInput);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
