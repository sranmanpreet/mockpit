package com.ms.utils.mockpit.auth.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ms.utils.mockpit.domain.AuthType;

/**
 * HTTP Basic auth. Stores a BCrypt password hash on disk; raw passwords from the UI are hashed by
 * {@code AuthConfigCodec} before persistence and never round-tripped back to the client.
 *
 * <p>{@link #password} is write-only on the wire (set by the UI on save, blank on read). {@link
 * #passwordHash} is the persisted form.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BasicAuthConfig extends AuthConfig {

    private String username;
    /** Plaintext password supplied by the UI on save; never returned. */
    private String password;
    /**
     * BCrypt hash actually stored on disk. {@code AuthConfigCodec#redactForResponse} blanks this
     * before any UI-bound serialisation, so the hash never crosses the wire to the browser even
     * though Jackson can read/write it for the JSON column.
     */
    private String passwordHash;
    private String realm = "mockpit";

    @Override public AuthType getType() { return AuthType.BASIC; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRealm() { return realm; }
    public void setRealm(String realm) { this.realm = realm; }
}
