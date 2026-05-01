package com.ms.utils.mockpit.auth;

import com.ms.utils.mockpit.auth.config.BasicAuthConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class BasicAuthValidatorTest {

    private BasicAuthValidator validator;
    private BasicAuthConfig cfg;

    @BeforeEach
    void setup() {
        validator = new BasicAuthValidator();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        ReflectionTestUtils.setField(validator, "passwordEncoder", encoder);
        cfg = new BasicAuthConfig();
        cfg.setUsername("alice");
        cfg.setPasswordHash(encoder.encode("CorrectHorseBatteryStaple"));
    }

    private MockHttpServletRequest req(String header) {
        MockHttpServletRequest r = new MockHttpServletRequest();
        if (header != null) r.addHeader("Authorization", header);
        return r;
    }

    private static String basic(String user, String pass) {
        return "Basic " + Base64.getEncoder()
                .encodeToString((user + ":" + pass).getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void successOnValidCredentials() {
        AuthValidationResult r = validator.validate(req(basic("alice", "CorrectHorseBatteryStaple")), cfg);
        assertThat(r.isSuccess()).isTrue();
    }

    @Test
    void failureOnWrongPassword() {
        AuthValidationResult r = validator.validate(req(basic("alice", "wrong")), cfg);
        assertThat(r.isFailure()).isTrue();
        assertThat(r.getWwwAuthenticate()).startsWith("Basic ");
    }

    @Test
    void failureOnMissingHeader() {
        AuthValidationResult r = validator.validate(req(null), cfg);
        assertThat(r.isFailure()).isTrue();
    }

    @Test
    void failureOnMalformedBase64() {
        AuthValidationResult r = validator.validate(req("Basic !!notbase64!!"), cfg);
        assertThat(r.isFailure()).isTrue();
    }

    @Test
    void failureOnMissingColon() {
        String bad = "Basic " + Base64.getEncoder().encodeToString("nocolon".getBytes(StandardCharsets.UTF_8));
        AuthValidationResult r = validator.validate(req(bad), cfg);
        assertThat(r.isFailure()).isTrue();
    }
}
