package com.ms.utils.mockpit.security;

/**
 * Lightweight principal cached on the security context once a JWT cookie has been validated.
 * Holds only what controllers need so we don't have to round-trip to the DB for the {@code AppUser}
 * on every request.
 */
public final class JwtPrincipal {
    private final Long userId;
    private final String email;
    private final String role;

    public JwtPrincipal(Long userId, String email, String role) {
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public boolean isAdmin() { return "ADMIN".equalsIgnoreCase(role); }

    @Override
    public String toString() { return email; }
}
