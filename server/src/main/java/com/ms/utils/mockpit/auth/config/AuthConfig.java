package com.ms.utils.mockpit.auth.config;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ms.utils.mockpit.domain.AuthType;

/**
 * Marker for the polymorphic per-mock auth configuration. Concrete subtypes are tagged with the
 * {@code type} discriminator on the JSON wire so the front-end can switch UI panels by value and
 * the back-end can pick the right validator.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = NoneAuthConfig.class, name = "NONE"),
        @JsonSubTypes.Type(value = BasicAuthConfig.class, name = "BASIC"),
        @JsonSubTypes.Type(value = JwtAuthConfig.class, name = "JWT"),
        @JsonSubTypes.Type(value = OAuth2RsAuthConfig.class, name = "OAUTH2_RS"),
        @JsonSubTypes.Type(value = OAuth2IntrospectAuthConfig.class, name = "OAUTH2_INTROSPECT")
})
public abstract class AuthConfig {
    public abstract AuthType getType();
}
