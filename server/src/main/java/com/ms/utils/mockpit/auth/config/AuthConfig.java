package com.ms.utils.mockpit.auth.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ms.utils.mockpit.domain.AuthType;

/**
 * Marker for the polymorphic per-mock auth configuration. Concrete subtypes are tagged with the
 * {@code type} discriminator on the JSON wire so the front-end can switch UI panels by value and
 * the back-end can pick the right validator.
 *
 * <p>{@code visible = true} keeps the discriminator visible in the JSON for the UI, but since the
 * subtypes don't expose a {@code setType} (the type is fixed by the class), we have to tell
 * Jackson not to barf when deserialising. {@code ignoreUnknown = true} is also a defensive
 * measure for any future fields the front-end may attach optimistically.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonIgnoreProperties(value = {"type"}, ignoreUnknown = true, allowGetters = true)
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
