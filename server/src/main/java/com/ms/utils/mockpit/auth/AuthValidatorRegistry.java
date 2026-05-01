package com.ms.utils.mockpit.auth;

import com.ms.utils.mockpit.domain.AuthType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Routes an {@link AuthType} to the right {@link AuthValidator} bean. Spring auto-discovers all
 * validators on the classpath and builds the map at startup; missing validators throw immediately.
 */
@Component
public class AuthValidatorRegistry {

    private final Map<AuthType, AuthValidator> validators = new EnumMap<>(AuthType.class);

    public AuthValidatorRegistry(List<AuthValidator> beans) {
        for (AuthValidator v : beans) {
            validators.put(v.supports(), v);
        }
    }

    public AuthValidator forType(AuthType type) {
        if (type == null || type == AuthType.NONE) return null;
        AuthValidator v = validators.get(type);
        if (v == null) {
            throw new IllegalStateException("No validator registered for auth type " + type);
        }
        return v;
    }
}
