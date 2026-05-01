package com.ms.utils.mockpit.auth;

import com.ms.utils.mockpit.auth.config.JwtAuthConfig;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthValidatorTest {

    private static final String SECRET = "test-jwt-secret-must-be-32-bytes-or-more-padding";
    private JwtAuthValidator validator;

    @BeforeEach
    void setup() {
        validator = new JwtAuthValidator();
        // No JwksCache needed for HS* tests; the field is only used for asymmetric flows.
        ReflectionTestUtils.setField(validator, "jwksCache", null);
    }

    private MockHttpServletRequest req(String authHeader) {
        MockHttpServletRequest r = new MockHttpServletRequest();
        if (authHeader != null) r.addHeader("Authorization", authHeader);
        return r;
    }

    private String issueHs256(String issuer, String audience, Instant exp, String scope) throws Exception {
        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .audience(audience)
                .expirationTime(Date.from(exp))
                .issueTime(new Date());
        if (scope != null) claims.claim("scope", scope);
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims.build());
        jwt.sign(new MACSigner(SECRET.getBytes(StandardCharsets.UTF_8)));
        return jwt.serialize();
    }

    @Test
    void successOnValidHs256Token() throws Exception {
        JwtAuthConfig cfg = new JwtAuthConfig();
        cfg.setAlgorithm("HS256");
        cfg.setSharedSecret(SECRET);
        cfg.setRequiredIssuer("https://issuer.example");
        cfg.setRequiredAudiences(List.of("api"));
        String token = issueHs256("https://issuer.example", "api", Instant.now().plusSeconds(60), "read write");

        AuthValidationResult r = validator.validate(req("Bearer " + token), cfg);
        assertThat(r.isSuccess()).isTrue();
    }

    @Test
    void rejectsExpiredToken() throws Exception {
        JwtAuthConfig cfg = new JwtAuthConfig();
        cfg.setAlgorithm("HS256");
        cfg.setSharedSecret(SECRET);
        cfg.setClockSkewSeconds(0);
        String token = issueHs256("iss", "api", Instant.now().minusSeconds(60), null);

        AuthValidationResult r = validator.validate(req("Bearer " + token), cfg);
        assertThat(r.isFailure()).isTrue();
        assertThat(r.getReason()).containsIgnoringCase("expired");
    }

    @Test
    void rejectsAlgorithmConfusion() throws Exception {
        // Token signed with HS256, config requires RS256 -> rejected before signature verify.
        JwtAuthConfig cfg = new JwtAuthConfig();
        cfg.setAlgorithm("RS256");
        cfg.setPublicKeyPem("-----BEGIN PUBLIC KEY-----\nfake\n-----END PUBLIC KEY-----");
        String token = issueHs256("iss", "api", Instant.now().plusSeconds(60), null);

        AuthValidationResult r = validator.validate(req("Bearer " + token), cfg);
        assertThat(r.isFailure()).isTrue();
    }

    @Test
    void rejectsWrongIssuer() throws Exception {
        JwtAuthConfig cfg = new JwtAuthConfig();
        cfg.setAlgorithm("HS256");
        cfg.setSharedSecret(SECRET);
        cfg.setRequiredIssuer("expected");
        String token = issueHs256("other", "api", Instant.now().plusSeconds(60), null);

        AuthValidationResult r = validator.validate(req("Bearer " + token), cfg);
        assertThat(r.isFailure()).isTrue();
        assertThat(r.getReason()).containsIgnoringCase("issuer");
    }

    @Test
    void rejectsMissingScope() throws Exception {
        JwtAuthConfig cfg = new JwtAuthConfig();
        cfg.setAlgorithm("HS256");
        cfg.setSharedSecret(SECRET);
        cfg.setRequiredScopes(List.of("admin"));
        String token = issueHs256("iss", "api", Instant.now().plusSeconds(60), "read");

        AuthValidationResult r = validator.validate(req("Bearer " + token), cfg);
        assertThat(r.isFailure()).isTrue();
        assertThat(r.getWwwAuthenticate()).contains("insufficient_scope");
    }

    @Test
    void rejectsTamperedSignature() throws Exception {
        JwtAuthConfig cfg = new JwtAuthConfig();
        cfg.setAlgorithm("HS256");
        cfg.setSharedSecret(SECRET);
        String token = issueHs256("iss", "api", Instant.now().plusSeconds(60), null);
        // Flip a character mid-signature; flipping the trailing base64url char alone may
        // be a no-op for HMAC-SHA256 because the last 2 bits are padding and lenient
        // base64url decoders (e.g. JJWT 0.12) treat 'A' and 'B' as the same byte sequence.
        int dotIdx = token.lastIndexOf('.');
        int mid = dotIdx + 1 + ((token.length() - dotIdx - 1) / 2);
        char c = token.charAt(mid);
        char swapped = c == 'A' ? 'b' : 'A';
        String tampered = token.substring(0, mid) + swapped + token.substring(mid + 1);
        AuthValidationResult r = validator.validate(req("Bearer " + tampered), cfg);
        assertThat(r.isFailure()).isTrue();
    }

    @Test
    void rejectsMissingAuthorizationHeader() {
        JwtAuthConfig cfg = new JwtAuthConfig();
        cfg.setAlgorithm("HS256");
        cfg.setSharedSecret(SECRET);
        AuthValidationResult r = validator.validate(req(null), cfg);
        assertThat(r.isFailure()).isTrue();
    }
}
