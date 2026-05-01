package com.ms.utils.mockpit.auth;

import com.ms.utils.mockpit.auth.config.AuthConfig;
import com.ms.utils.mockpit.auth.config.BasicAuthConfig;
import com.ms.utils.mockpit.domain.AuthType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class BasicAuthValidator implements AuthValidator {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public AuthType supports() { return AuthType.BASIC; }

    @Override
    public AuthValidationResult validate(HttpServletRequest request, AuthConfig cfg) {
        if (!(cfg instanceof BasicAuthConfig)) {
            return AuthValidationResult.failure(HttpStatus.INTERNAL_SERVER_ERROR, null, "Invalid Basic configuration.");
        }
        BasicAuthConfig basic = (BasicAuthConfig) cfg;
        String challenge = "Basic realm=\"" + (basic.getRealm() == null ? "mockpit" : basic.getRealm()) + "\"";

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.regionMatches(true, 0, "Basic ", 0, 6)) {
            return AuthValidationResult.failure(HttpStatus.UNAUTHORIZED, challenge,
                    "Missing or malformed Authorization header.");
        }
        String encoded = header.substring(6).trim();
        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return AuthValidationResult.failure(HttpStatus.UNAUTHORIZED, challenge,
                    "Authorization header is not valid base64.");
        }
        int colon = decoded.indexOf(':');
        if (colon <= 0 || colon == decoded.length() - 1) {
            return AuthValidationResult.failure(HttpStatus.UNAUTHORIZED, challenge,
                    "Authorization header malformed (expected user:password).");
        }
        String user = decoded.substring(0, colon);
        String pass = decoded.substring(colon + 1);

        // Constant-time username comparison to limit user-enumeration leaks.
        boolean userMatches = constantTimeEquals(user, basic.getUsername() == null ? "" : basic.getUsername());
        boolean passMatches = passwordEncoder.matches(pass, basic.getPasswordHash() == null ? "" : basic.getPasswordHash());

        if (userMatches && passMatches) {
            return AuthValidationResult.success();
        }
        return AuthValidationResult.failure(HttpStatus.UNAUTHORIZED, challenge, "Invalid Basic credentials.");
    }

    private static boolean constantTimeEquals(String a, String b) {
        byte[] ab = a.getBytes(StandardCharsets.UTF_8);
        byte[] bb = b.getBytes(StandardCharsets.UTF_8);
        int diff = ab.length ^ bb.length;
        for (int i = 0; i < Math.max(ab.length, bb.length); i++) {
            byte x = i < ab.length ? ab[i] : 0;
            byte y = i < bb.length ? bb[i] : 0;
            diff |= x ^ y;
        }
        return diff == 0;
    }
}
