package com.ms.utils.mockpit.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Convenience accessor for the principal cached on the Spring Security context. Returns
 * {@link Optional#empty()} when the request is unauthenticated. Controllers and services should
 * prefer this over manually reading {@link SecurityContextHolder} so the lookup logic stays in
 * one place.
 */
public final class CurrentUser {

    private CurrentUser() { }

    public static Optional<JwtPrincipal> get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return Optional.empty();
        Object principal = auth.getPrincipal();
        if (principal instanceof JwtPrincipal) return Optional.of((JwtPrincipal) principal);
        if (principal instanceof AuthenticatedUser) {
            AuthenticatedUser u = (AuthenticatedUser) principal;
            return Optional.of(new JwtPrincipal(u.getId(), u.getUsername(), u.getRole().name()));
        }
        return Optional.empty();
    }

    public static JwtPrincipal require() {
        return get().orElseThrow(() -> new IllegalStateException("No authenticated user on request."));
    }

    public static Long requireUserId() {
        return require().getUserId();
    }
}
