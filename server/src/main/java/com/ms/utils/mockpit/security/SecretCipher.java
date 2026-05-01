package com.ms.utils.mockpit.security;

import com.ms.utils.mockpit.config.MockpitProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Authenticated encryption (AES-256-GCM) for secrets stored at rest in the database.
 *
 * <p>Format on the wire/storage is a single base64 string of:
 *   {@code [ 1 byte version=1 | 12 bytes IV | ciphertext | 16 bytes GCM tag ]}.
 *
 * <p>Key material comes from {@code mockpit.security.secret-cipher-key}, which MUST be a base64
 * encoding of exactly 32 bytes. Validation runs at startup so misconfiguration fails-fast.
 */
@Component
public class SecretCipher {

    private static final String TRANSFORM = "AES/GCM/NoPadding";
    private static final byte VERSION = 1;
    private static final int IV_LEN = 12;
    private static final int TAG_BITS = 128;
    private static final SecureRandom RNG = new SecureRandom();

    @Autowired
    private MockpitProperties properties;

    private SecretKeySpec key;

    @PostConstruct
    public void init() {
        String b64 = properties.getSecurity().getSecretCipherKey();
        if (b64 == null || b64.isEmpty()) {
            throw new IllegalStateException("mockpit.security.secret-cipher-key is required.");
        }
        byte[] raw;
        try {
            raw = Base64.getDecoder().decode(b64);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("mockpit.security.secret-cipher-key must be base64.", ex);
        }
        if (raw.length != 32) {
            throw new IllegalStateException("mockpit.security.secret-cipher-key must decode to exactly 32 bytes (256 bits).");
        }
        this.key = new SecretKeySpec(raw, "AES");
    }

    public String encrypt(String plaintext) {
        if (plaintext == null) return null;
        try {
            byte[] iv = new byte[IV_LEN];
            RNG.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] ct = cipher.doFinal(plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            ByteBuffer bb = ByteBuffer.allocate(1 + iv.length + ct.length);
            bb.put(VERSION);
            bb.put(iv);
            bb.put(ct);
            return Base64.getEncoder().encodeToString(bb.array());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encrypt secret", e);
        }
    }

    public String decrypt(String ciphertextB64) {
        if (ciphertextB64 == null) return null;
        try {
            byte[] all = Base64.getDecoder().decode(ciphertextB64);
            if (all.length < 1 + IV_LEN + 16) {
                throw new IllegalStateException("Ciphertext too short.");
            }
            if (all[0] != VERSION) {
                throw new IllegalStateException("Unsupported ciphertext version: " + all[0]);
            }
            byte[] iv = new byte[IV_LEN];
            System.arraycopy(all, 1, iv, 0, IV_LEN);
            int ctOffset = 1 + IV_LEN;
            int ctLen = all.length - ctOffset;
            byte[] ct = new byte[ctLen];
            System.arraycopy(all, ctOffset, ct, 0, ctLen);

            Cipher cipher = Cipher.getInstance(TRANSFORM);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] pt = cipher.doFinal(ct);
            return new String(pt, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decrypt secret (key rotation issue?)", e);
        }
    }
}
