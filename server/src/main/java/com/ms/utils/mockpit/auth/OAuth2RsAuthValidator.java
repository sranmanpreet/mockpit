package com.ms.utils.mockpit.auth;

import com.ms.utils.mockpit.auth.config.AuthConfig;
import com.ms.utils.mockpit.auth.config.JwtAuthConfig;
import com.ms.utils.mockpit.auth.config.OAuth2RsAuthConfig;
import com.ms.utils.mockpit.auth.http.SafeHttpClient;
import com.ms.utils.mockpit.domain.AuthType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * OAuth2 Resource-Server validator. Translates {@link OAuth2RsAuthConfig} into a {@link JwtAuthConfig}
 * (resolving the JWKS URI from OIDC discovery if not supplied) and delegates to {@link
 * JwtAuthValidator}. Discovery results are cached in-process keyed by issuer.
 */
@Component
public class OAuth2RsAuthValidator implements AuthValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2RsAuthValidator.class);

    @Autowired
    private SafeHttpClient httpClient;

    @Autowired
    private JwtAuthValidator jwtAuthValidator;

    @Autowired
    private ObjectMapper objectMapper;

    private final ConcurrentMap<String, String> discoveryCache = new ConcurrentHashMap<>();

    @Override
    public AuthType supports() { return AuthType.OAUTH2_RS; }

    @Override
    public AuthValidationResult validate(HttpServletRequest request, AuthConfig cfg) {
        if (!(cfg instanceof OAuth2RsAuthConfig)) {
            return AuthValidationResult.failure(HttpStatus.INTERNAL_SERVER_ERROR, null,
                    "Invalid OAuth2 RS configuration.");
        }
        OAuth2RsAuthConfig oc = (OAuth2RsAuthConfig) cfg;

        String jwksUri = oc.getJwksUri();
        if (jwksUri == null || jwksUri.isEmpty()) {
            try {
                jwksUri = resolveJwksFromDiscovery(oc.getIssuer());
            } catch (Exception ex) {
                LOGGER.debug("OIDC discovery failed for {}: {}", oc.getIssuer(), ex.getMessage());
                return AuthValidationResult.failure(HttpStatus.UNAUTHORIZED,
                        "Bearer error=\"invalid_token\"", "Could not resolve JWKS for issuer.");
            }
        }
        JwtAuthConfig jwt = new JwtAuthConfig();
        jwt.setAlgorithm("RS256"); // OIDC default; the JwtAuthValidator will reject if header alg differs
        jwt.setJwksUri(jwksUri);
        jwt.setRequiredIssuer(oc.getIssuer());
        jwt.setRequiredAudiences(oc.getAudiences());
        jwt.setRequiredScopes(oc.getScopes());
        jwt.setClockSkewSeconds(oc.getClockSkewSeconds());

        // The header check inside JwtAuthValidator only allows the explicitly-configured algorithm.
        // For OIDC providers that mix RS256 and ES256 we attempt RS256 first; if a token uses a
        // different alg the resource owner should set jwksUri + algorithm explicitly via JwtAuthConfig.
        return jwtAuthValidator.validateInternal(request, jwt);
    }

    private String resolveJwksFromDiscovery(String issuer) throws Exception {
        if (issuer == null || issuer.isEmpty()) throw new IllegalArgumentException("issuer required");
        String cached = discoveryCache.get(issuer);
        if (cached != null) return cached;
        String url = issuer.endsWith("/")
                ? issuer + ".well-known/openid-configuration"
                : issuer + "/.well-known/openid-configuration";
        SafeHttpClient.Response resp = httpClient.getJson(url);
        if (resp.status / 100 != 2) {
            throw new IllegalStateException("OIDC discovery HTTP " + resp.status);
        }
        JsonNode tree = objectMapper.readTree(resp.body);
        String jwks = tree.path("jwks_uri").asText(null);
        if (jwks == null || jwks.isEmpty()) {
            throw new IllegalStateException("Discovery document missing jwks_uri.");
        }
        // Verify the discovery 'issuer' matches what we requested - prevents JWKS substitution.
        String discoveredIssuer = tree.path("issuer").asText(null);
        if (discoveredIssuer != null && !discoveredIssuer.equals(issuer)) {
            throw new IllegalStateException("Discovery issuer mismatch.");
        }
        discoveryCache.put(issuer, jwks);
        return jwks;
    }
}
