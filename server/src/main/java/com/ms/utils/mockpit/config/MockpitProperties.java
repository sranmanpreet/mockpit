package com.ms.utils.mockpit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

/**
 * Strongly-typed configuration for everything Mockpit-specific. Bound from {@code mockpit.*} keys
 * in {@code application.yml} / environment variables. All security-critical properties are annotated
 * so misconfiguration fails-fast at startup instead of at request time.
 */
@Component
@ConfigurationProperties("mockpit")
@Validated
public class MockpitProperties {

    @Valid
    private Cors cors = new Cors();

    @Valid
    private Security security = new Security();

    @Valid
    private RateLimit ratelimit = new RateLimit();

    @Valid
    private JsSandbox jsSandbox = new JsSandbox();

    @Valid
    private HttpClient httpClient = new HttpClient();

    public Cors getCors() { return cors; }
    public void setCors(Cors cors) { this.cors = cors; }
    public Security getSecurity() { return security; }
    public void setSecurity(Security security) { this.security = security; }
    public RateLimit getRatelimit() { return ratelimit; }
    public void setRatelimit(RateLimit ratelimit) { this.ratelimit = ratelimit; }
    public JsSandbox getJsSandbox() { return jsSandbox; }
    public void setJsSandbox(JsSandbox jsSandbox) { this.jsSandbox = jsSandbox; }
    public HttpClient getHttpClient() { return httpClient; }
    public void setHttpClient(HttpClient httpClient) { this.httpClient = httpClient; }

    public static class Cors {
        private List<String> allowedOrigins = Collections.emptyList();
        private List<String> allowedMethods = Collections.emptyList();
        private List<String> allowedHeaders = Collections.emptyList();
        private List<String> exposedHeaders = Collections.emptyList();
        private boolean allowCredentials = true;
        @Min(0)
        private long maxAge = 3600;

        public List<String> getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(List<String> v) { this.allowedOrigins = v; }
        public List<String> getAllowedMethods() { return allowedMethods; }
        public void setAllowedMethods(List<String> v) { this.allowedMethods = v; }
        public List<String> getAllowedHeaders() { return allowedHeaders; }
        public void setAllowedHeaders(List<String> v) { this.allowedHeaders = v; }
        public List<String> getExposedHeaders() { return exposedHeaders; }
        public void setExposedHeaders(List<String> v) { this.exposedHeaders = v; }
        public boolean isAllowCredentials() { return allowCredentials; }
        public void setAllowCredentials(boolean v) { this.allowCredentials = v; }
        public long getMaxAge() { return maxAge; }
        public void setMaxAge(long v) { this.maxAge = v; }
    }

    public static class Security {
        @Valid @NotNull
        private Jwt jwt = new Jwt();
        @NotBlank
        private String secretCipherKey;
        @Valid
        private BootstrapAdmin bootstrapAdmin = new BootstrapAdmin();

        public Jwt getJwt() { return jwt; }
        public void setJwt(Jwt v) { this.jwt = v; }
        public String getSecretCipherKey() { return secretCipherKey; }
        public void setSecretCipherKey(String v) { this.secretCipherKey = v; }
        public BootstrapAdmin getBootstrapAdmin() { return bootstrapAdmin; }
        public void setBootstrapAdmin(BootstrapAdmin v) { this.bootstrapAdmin = v; }
    }

    public static class Jwt {
        @NotBlank
        private String secret;
        @NotBlank
        private String issuer = "mockpit";
        @Min(60)
        private long accessTokenTtlSeconds = 900;
        @Min(60)
        private long refreshTokenTtlSeconds = 2592000;
        @NotBlank
        private String cookieName = "mockpit_session";
        private String cookieDomain = "";

        public String getSecret() { return secret; }
        public void setSecret(String v) { this.secret = v; }
        public String getIssuer() { return issuer; }
        public void setIssuer(String v) { this.issuer = v; }
        public long getAccessTokenTtlSeconds() { return accessTokenTtlSeconds; }
        public void setAccessTokenTtlSeconds(long v) { this.accessTokenTtlSeconds = v; }
        public long getRefreshTokenTtlSeconds() { return refreshTokenTtlSeconds; }
        public void setRefreshTokenTtlSeconds(long v) { this.refreshTokenTtlSeconds = v; }
        public String getCookieName() { return cookieName; }
        public void setCookieName(String v) { this.cookieName = v; }
        public String getCookieDomain() { return cookieDomain; }
        public void setCookieDomain(String v) { this.cookieDomain = v; }
    }

    public static class BootstrapAdmin {
        private boolean enabled = false;
        private String email = "";
        private String password = "";
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean v) { this.enabled = v; }
        public String getEmail() { return email; }
        public void setEmail(String v) { this.email = v; }
        public String getPassword() { return password; }
        public void setPassword(String v) { this.password = v; }
    }

    public static class RateLimit {
        private boolean enabled = true;
        @Min(1)
        private int adminRequestsPerMinute = 300;
        @Min(1)
        private int liveRequestsPerMinute = 600;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean v) { this.enabled = v; }
        public int getAdminRequestsPerMinute() { return adminRequestsPerMinute; }
        public void setAdminRequestsPerMinute(int v) { this.adminRequestsPerMinute = v; }
        public int getLiveRequestsPerMinute() { return liveRequestsPerMinute; }
        public void setLiveRequestsPerMinute(int v) { this.liveRequestsPerMinute = v; }
    }

    public static class JsSandbox {
        @Min(50)
        private long timeoutMs = 1000;
        @Min(1000)
        private long maxStatements = 100_000;
        @Min(1024)
        private int maxOutputBytes = 262_144;
        public long getTimeoutMs() { return timeoutMs; }
        public void setTimeoutMs(long v) { this.timeoutMs = v; }
        public long getMaxStatements() { return maxStatements; }
        public void setMaxStatements(long v) { this.maxStatements = v; }
        public int getMaxOutputBytes() { return maxOutputBytes; }
        public void setMaxOutputBytes(int v) { this.maxOutputBytes = v; }
    }

    public static class HttpClient {
        @Min(100)
        private int connectTimeoutMs = 3000;
        @Min(100)
        private int readTimeoutMs = 5000;
        private List<String> allowedHosts = Collections.emptyList();
        public int getConnectTimeoutMs() { return connectTimeoutMs; }
        public void setConnectTimeoutMs(int v) { this.connectTimeoutMs = v; }
        public int getReadTimeoutMs() { return readTimeoutMs; }
        public void setReadTimeoutMs(int v) { this.readTimeoutMs = v; }
        public List<String> getAllowedHosts() { return allowedHosts; }
        public void setAllowedHosts(List<String> v) { this.allowedHosts = v; }
    }
}
