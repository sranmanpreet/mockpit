package com.ms.utils.mockpit.auth.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ms.utils.mockpit.domain.AuthType;

import java.util.List;

/**
 * OAuth2 token introspection (RFC 7662). The mock-side validator POSTs the bearer token to the
 * introspection endpoint with its client credentials and accepts the response when {@code active}
 * is {@code true} and (optionally) the configured scopes/audiences are present.
 *
 * <p>{@link #clientSecret} is encrypted at rest by {@code AuthConfigCodec}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OAuth2IntrospectAuthConfig extends AuthConfig {

    private String introspectionUri;
    private String clientId;
    private String clientSecret;
    private List<String> requiredScopes;
    private List<String> requiredAudiences;
    /** Cache positive introspection responses for this many seconds (0 disables cache). */
    private long cacheTtlSeconds = 60;

    @Override public com.ms.utils.mockpit.domain.AuthType getType() {
        return com.ms.utils.mockpit.domain.AuthType.OAUTH2_INTROSPECT;
    }

    public String getIntrospectionUri() { return introspectionUri; }
    public void setIntrospectionUri(String v) { this.introspectionUri = v; }
    public String getClientId() { return clientId; }
    public void setClientId(String v) { this.clientId = v; }
    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String v) { this.clientSecret = v; }
    public List<String> getRequiredScopes() { return requiredScopes; }
    public void setRequiredScopes(List<String> v) { this.requiredScopes = v; }
    public List<String> getRequiredAudiences() { return requiredAudiences; }
    public void setRequiredAudiences(List<String> v) { this.requiredAudiences = v; }
    public long getCacheTtlSeconds() { return cacheTtlSeconds; }
    public void setCacheTtlSeconds(long v) { this.cacheTtlSeconds = v; }
}
