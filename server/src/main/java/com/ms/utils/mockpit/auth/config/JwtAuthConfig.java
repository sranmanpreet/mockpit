package com.ms.utils.mockpit.auth.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ms.utils.mockpit.domain.AuthType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT (Bearer) auth. Supports both shared-secret (HS256/384/512) and asymmetric (RS256/384/512,
 * ES256/384/512) variants. For asymmetric verification, supply either a {@link #publicKeyPem} or a
 * {@link #jwksUri} - the latter is preferred for OIDC providers.
 *
 * <p>Sensitive fields ({@link #sharedSecret} or {@link #publicKeyPem}) are encrypted at rest by
 * {@code AuthConfigCodec}; the plaintext form lives only in memory for the duration of a request.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JwtAuthConfig extends AuthConfig {

    /** "HS256" | "HS384" | "HS512" | "RS256" | "RS384" | "RS512" | "ES256" | "ES384" | "ES512" */
    private String algorithm = "HS256";

    /** HMAC secret for HS* algorithms. */
    private String sharedSecret;
    /** PEM-encoded public key for RS*&#47;ES* algorithms. */
    private String publicKeyPem;
    /** JWKS endpoint for OIDC-style key rotation. Mutually exclusive with {@link #publicKeyPem}. */
    private String jwksUri;

    private String requiredIssuer;
    private List<String> requiredAudiences;
    private List<String> requiredScopes;
    private Map<String, String> requiredClaims = new HashMap<>();
    /** Allowed clock-skew tolerance in seconds. */
    private long clockSkewSeconds = 60;
    /** Optional override of the header used to carry the token (default: Authorization). */
    private String headerName = "Authorization";
    /** Token prefix (default: "Bearer "). Set to empty string for raw tokens. */
    private String tokenPrefix = "Bearer ";

    @Override public AuthType getType() { return AuthType.JWT; }

    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String v) { this.algorithm = v; }
    public String getSharedSecret() { return sharedSecret; }
    public void setSharedSecret(String v) { this.sharedSecret = v; }
    public String getPublicKeyPem() { return publicKeyPem; }
    public void setPublicKeyPem(String v) { this.publicKeyPem = v; }
    public String getJwksUri() { return jwksUri; }
    public void setJwksUri(String v) { this.jwksUri = v; }
    public String getRequiredIssuer() { return requiredIssuer; }
    public void setRequiredIssuer(String v) { this.requiredIssuer = v; }
    public List<String> getRequiredAudiences() { return requiredAudiences; }
    public void setRequiredAudiences(List<String> v) { this.requiredAudiences = v; }
    public List<String> getRequiredScopes() { return requiredScopes; }
    public void setRequiredScopes(List<String> v) { this.requiredScopes = v; }
    public Map<String, String> getRequiredClaims() { return requiredClaims; }
    public void setRequiredClaims(Map<String, String> v) { this.requiredClaims = v; }
    public long getClockSkewSeconds() { return clockSkewSeconds; }
    public void setClockSkewSeconds(long v) { this.clockSkewSeconds = v; }
    public String getHeaderName() { return headerName; }
    public void setHeaderName(String v) { this.headerName = v; }
    public String getTokenPrefix() { return tokenPrefix; }
    public void setTokenPrefix(String v) { this.tokenPrefix = v; }
}
