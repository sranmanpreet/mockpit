package com.ms.utils.mockpit.web;

import com.ms.utils.mockpit.aop.exception.MockpitApplicationException;
import com.ms.utils.mockpit.config.MockpitProperties;
import com.ms.utils.mockpit.domain.AppUser;
import com.ms.utils.mockpit.repository.AppUserRepository;
import com.ms.utils.mockpit.security.AuthService;
import com.ms.utils.mockpit.security.CurrentUser;
import com.ms.utils.mockpit.security.JwtPrincipal;
import com.ms.utils.mockpit.security.JwtTokenService;
import com.ms.utils.mockpit.web.dto.AuthDtos;
import com.ms.utils.mockpit.web.dto.AuthDtos.LoginRequest;
import com.ms.utils.mockpit.web.dto.AuthDtos.MeResponse;
import com.ms.utils.mockpit.web.dto.AuthDtos.SignupRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Public auth surface: signup, login, logout, me. The session is carried as a JWT in an
 * HttpOnly+Secure cookie so the SPA never has to touch the token directly. Login is rate-limited
 * by the global {@code RateLimitFilter}.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private MockpitProperties properties;

    @PostMapping("/signup")
    public ResponseEntity<MeResponse> signup(@RequestBody SignupRequest request) throws MockpitApplicationException {
        AppUser user = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MeResponse(user.getId(), user.getEmail(), user.getDisplayName(),
                        user.getRole().name(), user.isEmailVerified()));
    }

    @PostMapping("/login")
    public ResponseEntity<MeResponse> login(@RequestBody LoginRequest request) throws MockpitApplicationException {
        if (request == null || request.email == null || request.password == null) {
            throw new MockpitApplicationException("Email and password are required.");
        }
        String email = request.email.trim().toLowerCase();
        AppUser user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        boolean credentialsValid = user != null
                && user.isEnabled()
                && !user.isCurrentlyLocked()
                && passwordEncoder.matches(request.password, user.getPasswordHash());

        if (!credentialsValid) {
            authService.recordFailedLogin(email);
            throw new MockpitApplicationException("Invalid credentials.");
        }
        authService.recordSuccessfulLogin(user);

        String token = jwtTokenService.issueAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        ResponseCookie cookie = buildSessionCookie(token,
                properties.getSecurity().getJwt().getAccessTokenTtlSeconds());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new MeResponse(user.getId(), user.getEmail(), user.getDisplayName(),
                        user.getRole().name(), user.isEmailVerified()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie clear = buildSessionCookie("", 0);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clear.toString())
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me() {
        JwtPrincipal p = CurrentUser.get().orElse(null);
        if (p == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        AppUser user = userRepository.findById(p.getUserId()).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(new MeResponse(user.getId(), user.getEmail(), user.getDisplayName(),
                user.getRole().name(), user.isEmailVerified()));
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<Map<String, String>> requestReset(@RequestBody AuthDtos.PasswordResetRequest req) {
        // We always return 202 to avoid disclosing whether an email exists. Sending the email is
        // a no-op stub for now; once SMTP is configured the AuthService can take over delivery.
        LOGGER.info("Password reset requested (stub)");
        Map<String, String> body = new HashMap<>();
        body.put("status", "If an account exists for that email, a reset link has been sent.");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(body);
    }

    @GetMapping("/csrf")
    public ResponseEntity<Map<String, String>> csrf() {
        // Spring Security will already have set the XSRF-TOKEN cookie at this point. Returning an
        // empty payload triggers cookie issuance for clients that pre-fetch.
        return ResponseEntity.ok(Collections.emptyMap());
    }

    private ResponseCookie buildSessionCookie(String value, long maxAgeSeconds) {
        MockpitProperties.Jwt jwt = properties.getSecurity().getJwt();
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(jwt.getCookieName(), value)
                .httpOnly(true)
                .secure(properties.getCors().isAllowCredentials() && isSecureContext())
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofSeconds(maxAgeSeconds));
        if (jwt.getCookieDomain() != null && !jwt.getCookieDomain().isEmpty()) {
            b.domain(jwt.getCookieDomain());
        }
        return b.build();
    }

    private boolean isSecureContext() {
        // In prod we always want secure cookies. application-prod.yml overrides this via property.
        return Boolean.parseBoolean(System.getProperty("MOCKPIT_COOKIE_SECURE",
                System.getenv().getOrDefault("MOCKPIT_COOKIE_SECURE", "false")));
    }
}
