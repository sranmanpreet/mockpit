package com.ms.utils.mockpit.security;

import com.ms.utils.mockpit.config.MockpitProperties;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenServiceTest {

    private JwtTokenService svc;

    @BeforeEach
    void setup() {
        MockpitProperties props = new MockpitProperties();
        props.getSecurity().getJwt().setSecret("test-jwt-secret-must-be-32-bytes-or-more-padding");
        props.getSecurity().getJwt().setIssuer("mockpit-test");
        props.getSecurity().getJwt().setAccessTokenTtlSeconds(60);
        svc = new JwtTokenService();
        ReflectionTestUtils.setField(svc, "properties", props);
        svc.init();
    }

    @Test
    void issuesAndParsesAccessToken() {
        String token = svc.issueAccessToken(7L, "alice@example.com", "USER");
        JwtTokenService.ParsedToken parsed = svc.parse(token);
        assertThat(parsed.userId).isEqualTo(7L);
        assertThat(parsed.email).isEqualTo("alice@example.com");
        assertThat(parsed.role).isEqualTo("USER");
    }

    @Test
    void rejectsTamperedToken() {
        String token = svc.issueAccessToken(7L, "alice@example.com", "USER");
        String tampered = token.substring(0, token.length() - 2) + "AA";
        assertThatThrownBy(() -> svc.parse(tampered)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsTokenWithWrongIssuer() {
        // Forge token signed with a different issuer.
        MockpitProperties otherProps = new MockpitProperties();
        otherProps.getSecurity().getJwt().setSecret("test-jwt-secret-must-be-32-bytes-or-more-padding");
        otherProps.getSecurity().getJwt().setIssuer("attacker-issuer");
        otherProps.getSecurity().getJwt().setAccessTokenTtlSeconds(60);
        JwtTokenService other = new JwtTokenService();
        ReflectionTestUtils.setField(other, "properties", otherProps);
        other.init();

        String foreignToken = other.issueAccessToken(1L, "evil@x", "ADMIN");
        assertThatThrownBy(() -> svc.parse(foreignToken)).isInstanceOf(JwtException.class);
    }

    @Test
    void requiresMinimumKeySize() {
        JwtTokenService bad = new JwtTokenService();
        MockpitProperties props = new MockpitProperties();
        props.getSecurity().getJwt().setSecret("too-short");
        ReflectionTestUtils.setField(bad, "properties", props);
        assertThatThrownBy(bad::init).isInstanceOf(IllegalStateException.class);
    }
}
