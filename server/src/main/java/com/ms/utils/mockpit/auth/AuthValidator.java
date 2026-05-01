package com.ms.utils.mockpit.auth;

import com.ms.utils.mockpit.auth.config.AuthConfig;
import com.ms.utils.mockpit.domain.AuthType;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Validates a single inbound mock request against the user-configured auth scheme. Implementations
 * are stateless Spring beans and dispatched via a map keyed by {@link AuthType}.
 */
public interface AuthValidator {

    AuthType supports();

    AuthValidationResult validate(HttpServletRequest request, AuthConfig config);
}
