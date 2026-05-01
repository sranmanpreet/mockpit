package com.ms.utils.mockpit.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms.utils.mockpit.aop.exception.MockpitApplicationException;
import com.ms.utils.mockpit.auth.config.AuthConfig;
import com.ms.utils.mockpit.auth.config.BasicAuthConfig;
import com.ms.utils.mockpit.auth.config.JwtAuthConfig;
import com.ms.utils.mockpit.auth.config.NoneAuthConfig;
import com.ms.utils.mockpit.auth.config.OAuth2IntrospectAuthConfig;
import com.ms.utils.mockpit.auth.config.OAuth2RsAuthConfig;
import com.ms.utils.mockpit.security.SecretCipher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Round-trips {@link AuthConfig} between the wire JSON, the database column and validator-facing
 * Java objects. Centralises three concerns:
 *
 * <ol>
 *   <li><b>Encryption-at-rest</b>: any field containing a long-lived secret (HMAC key, OAuth2
 *       client secret, PEM private/public key material) is encrypted via {@link SecretCipher}
 *       before being written to disk and decrypted on read. The plaintext form lives only inside
 *       memory for the duration of a request.</li>
 *   <li><b>Password hashing</b>: {@link BasicAuthConfig#getPassword()} (raw, write-only) is
 *       converted to a BCrypt hash via {@link PasswordEncoder} before persistence; the raw value
 *       is never written to the JSON column. On read, the hash is exposed to validators only -
 *       the wire form omits both fields.</li>
 *   <li><b>Validation of basic invariants</b>: incompatible field combinations (e.g. supplying
 *       both {@code sharedSecret} and {@code publicKeyPem} on a JWT config) are rejected here so
 *       the validator code can focus on the happy path.</li>
 * </ol>
 */
@Component
public class AuthConfigCodec {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SecretCipher secretCipher;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Encode an {@link AuthConfig} for persistence. Secrets are encrypted in-place; passwords are
     * hashed. Returns a JSON string ready to be stored in {@code mock.auth_config_json}.
     */
    public String encodeForStorage(AuthConfig cfg) throws MockpitApplicationException {
        if (cfg == null) cfg = new NoneAuthConfig();
        AuthConfig prepared = prepareForStorage(cfg);
        try {
            return objectMapper.writeValueAsString(prepared);
        } catch (IOException ex) {
            throw new MockpitApplicationException("Failed to serialise auth configuration.");
        }
    }

    /**
     * Decode a stored JSON blob into a runtime {@link AuthConfig}. Secrets are decrypted; password
     * hashes are exposed to validators but not to the JSON serialisation that goes back to the UI.
     */
    public AuthConfig decodeFromStorage(String json) {
        if (json == null || json.isEmpty()) return new NoneAuthConfig();
        try {
            AuthConfig raw = objectMapper.readValue(json, AuthConfig.class);
            return restoreFromStorage(raw);
        } catch (IOException ex) {
            throw new IllegalStateException("Stored auth_config_json is corrupt.", ex);
        }
    }

    /**
     * Sanitised view returned to the UI: secrets and password hashes are stripped so they cannot
     * be re-exfiltrated by a stolen browser session. Configuration metadata (issuer, JWKS URI,
     * scopes etc.) is preserved.
     */
    public AuthConfig redactForResponse(AuthConfig cfg) {
        if (cfg == null) return new NoneAuthConfig();
        try {
            AuthConfig clone = objectMapper.readValue(objectMapper.writeValueAsString(cfg), AuthConfig.class);
            redactInPlace(clone);
            return clone;
        } catch (IOException ex) {
            return new NoneAuthConfig();
        }
    }

    private AuthConfig prepareForStorage(AuthConfig cfg) throws MockpitApplicationException {
        if (cfg instanceof BasicAuthConfig) {
            BasicAuthConfig b = (BasicAuthConfig) cfg;
            if (isBlank(b.getUsername())) {
                throw new MockpitApplicationException("Basic auth requires a username.");
            }
            if (!isBlank(b.getPassword())) {
                b.setPasswordHash(passwordEncoder.encode(b.getPassword()));
            } else if (isBlank(b.getPasswordHash())) {
                throw new MockpitApplicationException("Basic auth requires a password.");
            }
            b.setPassword(null);
            return b;
        }
        if (cfg instanceof JwtAuthConfig) {
            JwtAuthConfig j = (JwtAuthConfig) cfg;
            String alg = j.getAlgorithm() == null ? "" : j.getAlgorithm().toUpperCase();
            boolean isHmac = alg.startsWith("HS");
            boolean hasSecret = !isBlank(j.getSharedSecret());
            boolean hasPem = !isBlank(j.getPublicKeyPem());
            boolean hasJwks = !isBlank(j.getJwksUri());
            if (isHmac && !hasSecret) {
                throw new MockpitApplicationException("JWT HS* algorithms require a sharedSecret.");
            }
            if (!isHmac && !(hasPem || hasJwks)) {
                throw new MockpitApplicationException("JWT RS*/ES* algorithms require either a publicKeyPem or jwksUri.");
            }
            if (hasPem && hasJwks) {
                throw new MockpitApplicationException("Provide either publicKeyPem or jwksUri, not both.");
            }
            if (hasSecret) j.setSharedSecret(secretCipher.encrypt(j.getSharedSecret()));
            if (hasPem) j.setPublicKeyPem(secretCipher.encrypt(j.getPublicKeyPem()));
            return j;
        }
        if (cfg instanceof OAuth2RsAuthConfig) {
            OAuth2RsAuthConfig o = (OAuth2RsAuthConfig) cfg;
            if (isBlank(o.getIssuer()) && isBlank(o.getJwksUri())) {
                throw new MockpitApplicationException("OAuth2 resource-server requires either issuer or jwksUri.");
            }
            return o;
        }
        if (cfg instanceof OAuth2IntrospectAuthConfig) {
            OAuth2IntrospectAuthConfig o = (OAuth2IntrospectAuthConfig) cfg;
            if (isBlank(o.getIntrospectionUri()) || isBlank(o.getClientId()) || isBlank(o.getClientSecret())) {
                throw new MockpitApplicationException("OAuth2 introspection requires introspectionUri, clientId and clientSecret.");
            }
            o.setClientSecret(secretCipher.encrypt(o.getClientSecret()));
            return o;
        }
        return cfg;
    }

    private AuthConfig restoreFromStorage(AuthConfig cfg) {
        if (cfg instanceof JwtAuthConfig) {
            JwtAuthConfig j = (JwtAuthConfig) cfg;
            if (!isBlank(j.getSharedSecret())) j.setSharedSecret(secretCipher.decrypt(j.getSharedSecret()));
            if (!isBlank(j.getPublicKeyPem())) j.setPublicKeyPem(secretCipher.decrypt(j.getPublicKeyPem()));
        } else if (cfg instanceof OAuth2IntrospectAuthConfig) {
            OAuth2IntrospectAuthConfig o = (OAuth2IntrospectAuthConfig) cfg;
            if (!isBlank(o.getClientSecret())) o.setClientSecret(secretCipher.decrypt(o.getClientSecret()));
        }
        return cfg;
    }

    private void redactInPlace(AuthConfig cfg) {
        if (cfg instanceof BasicAuthConfig) {
            BasicAuthConfig b = (BasicAuthConfig) cfg;
            b.setPassword(null);
            b.setPasswordHash(null);
        } else if (cfg instanceof JwtAuthConfig) {
            JwtAuthConfig j = (JwtAuthConfig) cfg;
            if (!isBlank(j.getSharedSecret())) j.setSharedSecret("***");
            if (!isBlank(j.getPublicKeyPem())) j.setPublicKeyPem("***");
        } else if (cfg instanceof OAuth2IntrospectAuthConfig) {
            OAuth2IntrospectAuthConfig o = (OAuth2IntrospectAuthConfig) cfg;
            if (!isBlank(o.getClientSecret())) o.setClientSecret("***");
        }
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}
