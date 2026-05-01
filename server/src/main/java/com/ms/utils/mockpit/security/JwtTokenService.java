package com.ms.utils.mockpit.security;

import com.ms.utils.mockpit.config.MockpitProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Issues and validates short-lived HMAC-signed JWTs that carry the user identity for the admin API.
 * Tokens are stored in an HttpOnly + Secure cookie ({@link MockpitProperties.Jwt#getCookieName()}),
 * and CSRF protection is enabled in {@code SecurityConfig} so the cookie alone cannot be replayed
 * cross-origin.
 *
 * <p>Algorithm: HS256, key derived from {@code mockpit.security.jwt.secret} which MUST be at least
 * 32 bytes of entropy (Spring fail-fast in prod via {@link MockpitProperties.Security}).
 */
@Service
public class JwtTokenService {

    public static final String CLAIM_ROLE = "role";

    @Autowired
    private MockpitProperties properties;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        byte[] secretBytes = properties.getSecurity().getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("mockpit.security.jwt.secret must be at least 32 bytes (256 bits) of entropy.");
        }
        this.signingKey = Keys.hmacShaKeyFor(secretBytes);
    }

    public String issueAccessToken(Long userId, String email, String role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(properties.getSecurity().getJwt().getAccessTokenTtlSeconds());
        return Jwts.builder()
                .setIssuer(properties.getSecurity().getJwt().getIssuer())
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .claim(CLAIM_ROLE, role)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public ParsedToken parse(String token) throws JwtException {
        Jws<Claims> jws = Jwts.parserBuilder()
                .requireIssuer(properties.getSecurity().getJwt().getIssuer())
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token);
        Claims body = jws.getBody();
        return new ParsedToken(
                Long.parseLong(body.getSubject()),
                String.valueOf(body.get("email")),
                String.valueOf(body.get(CLAIM_ROLE))
        );
    }

    public static class ParsedToken {
        public final Long userId;
        public final String email;
        public final String role;
        public ParsedToken(Long userId, String email, String role) {
            this.userId = userId;
            this.email = email;
            this.role = role;
        }
    }
}
