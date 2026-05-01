package com.ms.utils.mockpit.auth;

import com.ms.utils.mockpit.auth.config.AuthConfig;
import com.ms.utils.mockpit.auth.config.JwtAuthConfig;
import com.ms.utils.mockpit.auth.jwks.JwksCache;
import com.ms.utils.mockpit.domain.AuthType;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Validates JWTs (HS / RS / ES) according to {@link JwtAuthConfig}. Performs:
 *
 * <ol>
 *   <li>Header parsing and algorithm allow-checking. The {@code alg=none} attack is rejected
 *       implicitly because Nimbus' {@code SignedJWT.parse} requires a signature segment, but we
 *       also explicitly reject any algorithm not matching the configured one (CVE-class:
 *       algorithm confusion / header tampering).</li>
 *   <li>Signature verification against either a shared HMAC secret, a static public key, or a key
 *       resolved from a JWKS URI by {@code kid}.</li>
 *   <li>Claim checks: issuer, audience (any-match), expiry / not-before with configurable clock
 *       skew, and arbitrary {@code requiredClaims} key-equals-value pairs.</li>
 *   <li>Scope check: the JWT {@code scope} or {@code scp} claim must contain ALL configured
 *       {@code requiredScopes}.</li>
 * </ol>
 */
@Component
public class JwtAuthValidator implements AuthValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthValidator.class);
    private static final Set<String> SUPPORTED_ALGS = Set.of(
            "HS256", "HS384", "HS512",
            "RS256", "RS384", "RS512",
            "ES256", "ES384", "ES512");

    @Autowired
    private JwksCache jwksCache;

    @Override
    public AuthType supports() { return AuthType.JWT; }

    @Override
    public AuthValidationResult validate(HttpServletRequest request, AuthConfig cfg) {
        if (!(cfg instanceof JwtAuthConfig)) {
            return AuthValidationResult.failure(HttpStatus.INTERNAL_SERVER_ERROR, null, "Invalid JWT configuration.");
        }
        JwtAuthConfig jwt = (JwtAuthConfig) cfg;

        String headerName = jwt.getHeaderName() == null ? HttpHeaders.AUTHORIZATION : jwt.getHeaderName();
        String prefix = jwt.getTokenPrefix() == null ? "Bearer " : jwt.getTokenPrefix();

        String token = extractToken(request, headerName, prefix);
        if (token == null) {
            return AuthValidationResult.failure(HttpStatus.UNAUTHORIZED,
                    bearerChallenge(null, "missing_token"), "Missing token.");
        }

        SignedJWT parsed;
        try {
            parsed = SignedJWT.parse(token);
        } catch (Exception e) {
            return AuthValidationResult.failure(HttpStatus.UNAUTHORIZED,
                    bearerChallenge(jwt.getRequiredIssuer(), "invalid_token"), "Token is not a valid JWS.");
        }

        String configuredAlg = jwt.getAlgorithm() == null ? "" : jwt.getAlgorithm().toUpperCase();
        if (!SUPPORTED_ALGS.contains(configuredAlg)) {
            return AuthValidationResult.failure(HttpStatus.INTERNAL_SERVER_ERROR, null,
                    "Configured algorithm '" + configuredAlg + "' is not supported.");
        }
        JWSHeader header = parsed.getHeader();
        if (!configuredAlg.equals(header.getAlgorithm().getName())) {
            // Algorithm-confusion guard.
            return AuthValidationResult.failure(HttpStatus.UNAUTHORIZED,
                    bearerChallenge(jwt.getRequiredIssuer(), "invalid_token"),
                    "Token alg '" + header.getAlgorithm().getName() + "' does not match configured '"
                            + configuredAlg + "'.");
        }

        try {
            JWSVerifier verifier = buildVerifier(jwt, parsed.getHeader());
            if (!parsed.verify(verifier)) {
                return AuthValidationResult.failure(HttpStatus.UNAUTHORIZED,
                        bearerChallenge(jwt.getRequiredIssuer(), "invalid_token"), "Signature verification failed.");
            }
        } catch (Exception e) {
            LOGGER.debug("JWT verification error: {}", e.getClass().getSimpleName());
            return AuthValidationResult.failure(HttpStatus.UNAUTHORIZED,
                    bearerChallenge(jwt.getRequiredIssuer(), "invalid_token"), "Signature verification failed.");
        }

        try {
            JWTClaimsSet claims = parsed.getJWTClaimsSet();
            AuthValidationResult claimCheck = checkClaims(claims, jwt);
            if (claimCheck.isFailure()) return claimCheck;
            return AuthValidationResult.success(toMap(claims));
        } catch (Exception e) {
            return AuthValidationResult.failure(HttpStatus.UNAUTHORIZED,
                    bearerChallenge(jwt.getRequiredIssuer(), "invalid_token"), "Could not parse JWT claims.");
        }
    }

    private JWSVerifier buildVerifier(JwtAuthConfig jwt, JWSHeader header) throws JOSEException {
        String alg = jwt.getAlgorithm().toUpperCase();
        if (alg.startsWith("HS")) {
            byte[] keyBytes = jwt.getSharedSecret().getBytes(StandardCharsets.UTF_8);
            // HS256 needs >= 32 bytes; if shorter, MACVerifier will throw which we map to a failure.
            return new MACVerifier(keyBytes);
        }
        JWK jwk;
        if (jwt.getJwksUri() != null && !jwt.getJwksUri().isEmpty()) {
            JWKSet keys;
            try {
                keys = jwksCache.get(jwt.getJwksUri());
            } catch (Exception ex) {
                throw new JOSEException("JWKS fetch failed: " + ex.getMessage(), ex);
            }
            String kid = header.getKeyID();
            jwk = kid == null ? keys.getKeys().get(0) : keys.getKeyByKeyId(kid);
            if (jwk == null) throw new JOSEException("No matching key for kid=" + kid);
        } else {
            try {
                jwk = JWK.parseFromPEMEncodedObjects(jwt.getPublicKeyPem());
            } catch (Exception ex) {
                throw new JOSEException("Invalid PEM-encoded public key.", ex);
            }
        }
        if (alg.startsWith("RS")) {
            RSAKey rsa = jwk.toRSAKey();
            return new RSASSAVerifier((RSAPublicKey) rsa.toPublicKey());
        }
        if (alg.startsWith("ES")) {
            ECKey ec = jwk.toECKey();
            return new ECDSAVerifier((ECPublicKey) ec.toPublicKey());
        }
        throw new JOSEException("Unsupported algorithm: " + alg);
    }

    private AuthValidationResult checkClaims(JWTClaimsSet claims, JwtAuthConfig jwt) {
        long now = Instant.now().getEpochSecond();
        long skew = Math.max(0, jwt.getClockSkewSeconds());

        Date exp = claims.getExpirationTime();
        if (exp == null) {
            return AuthValidationResult.failure(HttpStatus.UNAUTHORIZED,
                    bearerChallenge(jwt.getRequiredIssuer(), "invalid_token"), "Token has no exp claim.");
        }
        if (exp.toInstant().getEpochSecond() + skew < now) {
            return AuthValidationResult.failure(HttpStatus.UNAUTHORIZED,
                    bearerChallenge(jwt.getRequiredIssuer(), "invalid_token"), "Token is expired.");
        }
        Date nbf = claims.getNotBeforeTime();
        if (nbf != null && nbf.toInstant().getEpochSecond() - skew > now) {
            return AuthValidationResult.failure(HttpStatus.UNAUTHORIZED,
                    bearerChallenge(jwt.getRequiredIssuer(), "invalid_token"), "Token not yet valid.");
        }
        if (jwt.getRequiredIssuer() != null && !jwt.getRequiredIssuer().isEmpty()) {
            if (!jwt.getRequiredIssuer().equals(claims.getIssuer())) {
                return AuthValidationResult.failure(HttpStatus.UNAUTHORIZED,
                        bearerChallenge(jwt.getRequiredIssuer(), "invalid_token"), "Issuer mismatch.");
            }
        }
        if (jwt.getRequiredAudiences() != null && !jwt.getRequiredAudiences().isEmpty()) {
            List<String> tokenAud = claims.getAudience();
            boolean matched = tokenAud != null && tokenAud.stream().anyMatch(jwt.getRequiredAudiences()::contains);
            if (!matched) {
                return AuthValidationResult.failure(HttpStatus.UNAUTHORIZED,
                        bearerChallenge(jwt.getRequiredIssuer(), "invalid_token"), "Audience mismatch.");
            }
        }
        if (jwt.getRequiredScopes() != null && !jwt.getRequiredScopes().isEmpty()) {
            String scopeClaim = readStringOrJoinedList(claims, "scope", "scp");
            if (scopeClaim == null) {
                return AuthValidationResult.failure(HttpStatus.UNAUTHORIZED,
                        insufficientScope(jwt.getRequiredScopes()), "Token has no scope.");
            }
            Set<String> tokenScopes = Set.of(scopeClaim.split("\\s+"));
            if (!tokenScopes.containsAll(jwt.getRequiredScopes())) {
                return AuthValidationResult.failure(HttpStatus.FORBIDDEN,
                        insufficientScope(jwt.getRequiredScopes()), "Required scope missing.");
            }
        }
        if (jwt.getRequiredClaims() != null) {
            for (Map.Entry<String, String> e : jwt.getRequiredClaims().entrySet()) {
                Object v = claims.getClaim(e.getKey());
                if (v == null || !String.valueOf(v).equals(e.getValue())) {
                    return AuthValidationResult.failure(HttpStatus.UNAUTHORIZED,
                            bearerChallenge(jwt.getRequiredIssuer(), "invalid_token"),
                            "Required claim '" + e.getKey() + "' mismatch.");
                }
            }
        }
        return AuthValidationResult.success();
    }

    private static String readStringOrJoinedList(JWTClaimsSet claims, String... keys) {
        for (String k : keys) {
            Object v = claims.getClaim(k);
            if (v == null) continue;
            if (v instanceof String) return (String) v;
            if (v instanceof List) {
                StringBuilder sb = new StringBuilder();
                for (Object o : (List<?>) v) {
                    if (sb.length() > 0) sb.append(' ');
                    sb.append(o);
                }
                return sb.toString();
            }
        }
        return null;
    }

    private static String extractToken(HttpServletRequest request, String headerName, String prefix) {
        String h = request.getHeader(headerName);
        if (h == null) return null;
        if (prefix == null || prefix.isEmpty()) return h.trim();
        if (!h.regionMatches(true, 0, prefix, 0, prefix.length())) return null;
        return h.substring(prefix.length()).trim();
    }

    private static String bearerChallenge(String issuer, String error) {
        StringBuilder sb = new StringBuilder("Bearer");
        if (issuer != null && !issuer.isEmpty()) {
            sb.append(" realm=\"").append(issuer.replace("\"", "")).append('"');
        }
        if (error != null) {
            sb.append(", error=\"").append(error).append('"');
        }
        return sb.toString();
    }

    private static String insufficientScope(List<String> scopes) {
        return "Bearer error=\"insufficient_scope\", scope=\"" + String.join(" ", scopes) + "\"";
    }

    private static Map<String, Object> toMap(JWTClaimsSet claims) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.putAll(new HashMap<>(claims.getClaims()));
        return result;
    }

    /** Exposed so the OAuth2 RS validator can delegate signature checks here. */
    public AuthValidationResult validateInternal(HttpServletRequest request, JwtAuthConfig cfg) {
        return validate(request, cfg);
    }
}
