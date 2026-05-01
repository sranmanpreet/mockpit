package com.ms.utils.mockpit.security;

import com.ms.utils.mockpit.config.MockpitProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecretCipherTest {

    private SecretCipher cipher;

    @BeforeEach
    void setup() {
        MockpitProperties props = new MockpitProperties();
        // 32 bytes of zeros, base64-encoded.
        props.getSecurity().setSecretCipherKey(Base64.getEncoder().encodeToString(new byte[32]));
        cipher = new SecretCipher();
        ReflectionTestUtils.setField(cipher, "properties", props);
        cipher.init();
    }

    @Test
    void roundTripsArbitraryStrings() {
        String[] samples = {"", "a", "shared-jwt-secret-32-bytes-of-data", "non-ascii: \u00fc\u00e9 \uD83D\uDE00",
                "a".repeat(10_000)};
        for (String s : samples) {
            String enc = cipher.encrypt(s);
            assertThat(enc).isNotEqualTo(s);
            assertThat(cipher.decrypt(enc)).isEqualTo(s);
        }
    }

    @Test
    void encryptionIsNonDeterministic() {
        String enc1 = cipher.encrypt("same-input");
        String enc2 = cipher.encrypt("same-input");
        assertThat(enc1).isNotEqualTo(enc2);
        assertThat(cipher.decrypt(enc1)).isEqualTo("same-input");
        assertThat(cipher.decrypt(enc2)).isEqualTo("same-input");
    }

    @Test
    void rejectsTamperedCiphertext() {
        String enc = cipher.encrypt("payload");
        byte[] raw = Base64.getDecoder().decode(enc);
        // Flip a bit in the ciphertext (skip version byte + 12-byte IV).
        raw[15] ^= 0x40;
        String tampered = Base64.getEncoder().encodeToString(raw);
        assertThatThrownBy(() -> cipher.decrypt(tampered)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsBadKeyOnInit() {
        SecretCipher bad = new SecretCipher();
        MockpitProperties props = new MockpitProperties();
        props.getSecurity().setSecretCipherKey(Base64.getEncoder().encodeToString(new byte[16]));
        ReflectionTestUtils.setField(bad, "properties", props);
        assertThatThrownBy(bad::init).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 bytes");
    }
}
