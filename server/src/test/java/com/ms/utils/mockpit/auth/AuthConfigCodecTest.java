package com.ms.utils.mockpit.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms.utils.mockpit.aop.exception.MockpitApplicationException;
import com.ms.utils.mockpit.auth.config.AuthConfig;
import com.ms.utils.mockpit.auth.config.BasicAuthConfig;
import com.ms.utils.mockpit.auth.config.JwtAuthConfig;
import com.ms.utils.mockpit.auth.config.NoneAuthConfig;
import com.ms.utils.mockpit.config.MockpitProperties;
import com.ms.utils.mockpit.security.SecretCipher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthConfigCodecTest {

    private AuthConfigCodec codec;
    private SecretCipher cipher;
    private ObjectMapper mapper;
    private BCryptPasswordEncoder encoder;

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper();
        cipher = new SecretCipher();
        MockpitProperties props = new MockpitProperties();
        props.getSecurity().setSecretCipherKey(Base64.getEncoder().encodeToString(new byte[32]));
        ReflectionTestUtils.setField(cipher, "properties", props);
        cipher.init();
        encoder = new BCryptPasswordEncoder();

        codec = new AuthConfigCodec();
        ReflectionTestUtils.setField(codec, "objectMapper", mapper);
        ReflectionTestUtils.setField(codec, "secretCipher", cipher);
        ReflectionTestUtils.setField(codec, "passwordEncoder", encoder);
    }

    @Test
    void noneAuthRoundTripsAsNull() throws Exception {
        String json = codec.encodeForStorage(new NoneAuthConfig());
        AuthConfig back = codec.decodeFromStorage(json);
        assertThat(back).isInstanceOf(NoneAuthConfig.class);
    }

    @Test
    void basicHashesPasswordAndStripsPlaintext() throws Exception {
        BasicAuthConfig b = new BasicAuthConfig();
        b.setUsername("alice");
        b.setPassword("CorrectHorseBatteryStaple");
        String json = codec.encodeForStorage(b);
        assertThat(json).doesNotContain("CorrectHorseBatteryStaple");
        AuthConfig back = codec.decodeFromStorage(json);
        assertThat(back).isInstanceOf(BasicAuthConfig.class);
        BasicAuthConfig restored = (BasicAuthConfig) back;
        assertThat(restored.getPassword()).isNull();
        assertThat(encoder.matches("CorrectHorseBatteryStaple", restored.getPasswordHash())).isTrue();
    }

    @Test
    void basicRequiresUsernameAndPassword() {
        BasicAuthConfig b = new BasicAuthConfig();
        assertThatThrownBy(() -> codec.encodeForStorage(b))
                .isInstanceOf(MockpitApplicationException.class)
                .hasMessageContaining("username");

        BasicAuthConfig b2 = new BasicAuthConfig();
        b2.setUsername("alice");
        assertThatThrownBy(() -> codec.encodeForStorage(b2))
                .isInstanceOf(MockpitApplicationException.class)
                .hasMessageContaining("password");
    }

    @Test
    void jwtSecretIsEncryptedOnStorage() throws Exception {
        JwtAuthConfig j = new JwtAuthConfig();
        j.setAlgorithm("HS256");
        j.setSharedSecret("plaintext-secret-32-bytes-of-data!!");
        String json = codec.encodeForStorage(j);
        assertThat(json).doesNotContain("plaintext-secret");
        JwtAuthConfig back = (JwtAuthConfig) codec.decodeFromStorage(json);
        assertThat(back.getSharedSecret()).isEqualTo("plaintext-secret-32-bytes-of-data!!");
    }

    @Test
    void redactedConfigDoesNotLeakSecrets() throws Exception {
        BasicAuthConfig b = new BasicAuthConfig();
        b.setUsername("alice");
        b.setPassword("CorrectHorseBatteryStaple");
        String json = codec.encodeForStorage(b);
        AuthConfig restored = codec.decodeFromStorage(json);
        AuthConfig redacted = codec.redactForResponse(restored);
        BasicAuthConfig r = (BasicAuthConfig) redacted;
        assertThat(r.getPassword()).isNull();
        assertThat(r.getPasswordHash()).isNull();
    }

    @Test
    void jwtRejectsBothPemAndJwks() {
        JwtAuthConfig j = new JwtAuthConfig();
        j.setAlgorithm("RS256");
        j.setPublicKeyPem("-----BEGIN PUBLIC KEY-----\nfake\n-----END PUBLIC KEY-----");
        j.setJwksUri("https://example/jwks");
        assertThatThrownBy(() -> codec.encodeForStorage(j))
                .isInstanceOf(MockpitApplicationException.class)
                .hasMessageContaining("either");
    }
}
