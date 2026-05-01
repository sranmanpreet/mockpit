package com.ms.utils.mockpit.domain;

/**
 * Authentication scheme attached to a mock. {@link #NONE} keeps the pre-2.0 behaviour
 * (mock served without any authentication check) and is the default for backwards compatibility.
 */
public enum AuthType {
    NONE,
    BASIC,
    JWT,
    OAUTH2_RS,
    OAUTH2_INTROSPECT
}
