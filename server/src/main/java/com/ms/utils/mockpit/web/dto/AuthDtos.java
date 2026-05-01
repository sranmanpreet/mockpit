package com.ms.utils.mockpit.web.dto;

/**
 * Request and response payloads for {@code AuthController}. Plain POJOs to keep the API surface
 * obvious; validation rules live in the service layer where they have access to side-effects.
 */
public final class AuthDtos {

    private AuthDtos() { }

    public static class SignupRequest {
        public String email;
        public String password;
        public String displayName;
    }

    public static class LoginRequest {
        public String email;
        public String password;
    }

    public static class MeResponse {
        public Long id;
        public String email;
        public String displayName;
        public String role;
        public boolean emailVerified;
        public MeResponse() { }
        public MeResponse(Long id, String email, String displayName, String role, boolean emailVerified) {
            this.id = id;
            this.email = email;
            this.displayName = displayName;
            this.role = role;
            this.emailVerified = emailVerified;
        }
    }

    public static class PasswordResetRequest {
        public String email;
    }

    public static class PasswordResetConfirm {
        public String token;
        public String newPassword;
    }
}
