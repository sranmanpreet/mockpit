package com.ms.utils.mockpit.auth.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ms.utils.mockpit.domain.AuthType;

import java.util.List;

/**
 * OAuth2 Resource-Server style validation. The bearer token is expected to be a JWT signed by the
 * issuer; verification keys are retrieved from the issuer's JWKS endpoint (discovered via
 * {@code /.well-known/openid-configuration} unless {@link #jwksUri} is set explicitly).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OAuth2RsAuthConfig extends AuthConfig {

    private String issuer;
    private String jwksUri;
    private List<String> audiences;
    private List<String> scopes;
    private long clockSkewSeconds = 60;

    @Override public AuthType getType() { return AuthType.OAUTH2_RS; }

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
    public String getJwksUri() { return jwksUri; }
    public void setJwksUri(String jwksUri) { this.jwksUri = jwksUri; }
    public List<String> getAudiences() { return audiences; }
    public void setAudiences(List<String> audiences) { this.audiences = audiences; }
    public List<String> getScopes() { return scopes; }
    public void setScopes(List<String> scopes) { this.scopes = scopes; }
    public long getClockSkewSeconds() { return clockSkewSeconds; }
    public void setClockSkewSeconds(long clockSkewSeconds) { this.clockSkewSeconds = clockSkewSeconds; }
}
