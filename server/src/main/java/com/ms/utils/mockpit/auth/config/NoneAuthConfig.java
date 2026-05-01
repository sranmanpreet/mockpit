package com.ms.utils.mockpit.auth.config;

import com.ms.utils.mockpit.domain.AuthType;

/**
 * No authentication required. Default for backwards compatibility.
 */
public class NoneAuthConfig extends AuthConfig {
    @Override
    public AuthType getType() {
        return AuthType.NONE;
    }
}
