package com.ms.utils.mockpit.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ms.utils.mockpit.auth.config.AuthConfig;
import com.ms.utils.mockpit.auth.config.OAuth2IntrospectAuthConfig;
import com.ms.utils.mockpit.auth.http.SafeHttpClient;
import com.ms.utils.mockpit.domain.AuthType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * RFC 7662 token introspection. Sends the bearer token to the configured introspection endpoint
 * with the configured client credentials and accepts the response when {@code active=true}, the
 * configured scopes are present, and (optionally) the configured audience matches.
 *
 * <p>Positive responses are cached by SHA-256(token) for the configured TTL to avoid hammering the
 * AS on every request. Negative responses are <em>not</em> cached so revocation takes effect on
 * the next attempt.
 */
@Component
public class OAuth2IntrospectAuthValidator implements AuthValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2IntrospectAuthValidator.class);

    @Autowired
    private SafeHttpClient httpClient;

    @Autowired
    private ObjectMapper objectMapper;

    private final Cache<String, Boolean> tokenCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofMinutes(5))
            .build();

    @Override
    public AuthType supports() { return AuthType.OAUTH2_INTROSPECT; }

    @Override
    public AuthValidationResult validate(HttpServletRequest request, AuthConfig cfg) {
        if (!(cfg instanceof OAuth2IntrospectAuthConfig)) {
            return AuthValidationResult.failure(HttpStatus.INTERNAL_SERVER_ERROR, null,
                    "Invalid OAuth2 introspection configuration.");
        }
        OAuth2IntrospectAuthConfig oc = (OAuth2IntrospectAuthConfig) cfg;

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return AuthValidationResult.failure(HttpStatus.UNAUTHORIZED,
                    "Bearer error=\"invalid_token\"", "Missing bearer token.");
        }
        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            return AuthValidationResult.failure(HttpStatus.UNAUTHORIZED,
                    "Bearer error=\"invalid_token\"", "Empty bearer token.");
        }

        String cacheKey = sha256(token);
        Boolean cached = tokenCache.getIfPresent(cacheKey);
        if (Boolean.TRUE.equals(cached)) {
            return AuthValidationResult.success();
        }

        String basic = Base64.getEncoder().encodeToString(
                (oc.getClientId() + ":" + oc.getClientSecret()).getBytes(StandardCharsets.UTF_8));
        String body = "token=" + URLEncoder.encode(token, StandardCharsets.UTF_8)
                + "&token_type_hint=access_token";
        SafeHttpClient.Response resp;
        try {
            resp = httpClient.postForm(oc.getIntrospectionUri(), body, basic);
        } catch (Exception ex) {
            LOGGER.debug("Introspection call failed: {}", ex.getMessage());
            return AuthValidationResult.failure(HttpStatus.UNAUTHORIZED,
                    "Bearer error=\"invalid_token\"", "Introspection endpoint unreachable.");
        }
        if (resp.status / 100 != 2) {
            return AuthValidationResult.failure(HttpStatus.UNAUTHORIZED,
                    "Bearer error=\"invalid_token\"", "Introspection HTTP " + resp.status);
        }
        try {
            JsonNode tree = objectMapper.readTree(resp.body);
            if (!tree.path("active").asBoolean(false)) {
                return AuthValidationResult.failure(HttpStatus.UNAUTHORIZED,
                        "Bearer error=\"invalid_token\"", "Token is not active.");
            }
            if (oc.getRequiredScopes() != null && !oc.getRequiredScopes().isEmpty()) {
                Set<String> tokenScopes = parseScopeNode(tree.path("scope"));
                if (!tokenScopes.containsAll(oc.getRequiredScopes())) {
                    return AuthValidationResult.failure(HttpStatus.FORBIDDEN,
                            "Bearer error=\"insufficient_scope\", scope=\""
                                    + String.join(" ", oc.getRequiredScopes()) + "\"",
                            "Required scope missing.");
                }
            }
            if (oc.getRequiredAudiences() != null && !oc.getRequiredAudiences().isEmpty()) {
                JsonNode audNode = tree.path("aud");
                Set<String> auds = new HashSet<>();
                if (audNode.isArray()) {
                    audNode.forEach(n -> auds.add(n.asText()));
                } else if (audNode.isTextual()) {
                    auds.add(audNode.asText());
                }
                boolean ok = oc.getRequiredAudiences().stream().anyMatch(auds::contains);
                if (!ok) {
                    return AuthValidationResult.failure(HttpStatus.UNAUTHORIZED,
                            "Bearer error=\"invalid_token\"", "Audience mismatch.");
                }
            }
            if (oc.getCacheTtlSeconds() > 0) {
                tokenCache.put(cacheKey, Boolean.TRUE);
            }
            return AuthValidationResult.success();
        } catch (Exception ex) {
            return AuthValidationResult.failure(HttpStatus.UNAUTHORIZED,
                    "Bearer error=\"invalid_token\"", "Could not parse introspection response.");
        }
    }

    private static Set<String> parseScopeNode(JsonNode node) {
        Set<String> out = new HashSet<>();
        if (node == null || node.isMissingNode() || node.isNull()) return out;
        if (node.isArray()) {
            node.forEach(n -> out.add(n.asText()));
            return out;
        }
        if (node.isTextual()) {
            for (String s : node.asText("").split("\\s+")) {
                if (!s.isEmpty()) out.add(s);
            }
        }
        return out;
    }

    private static String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return s;
        }
    }
}
