package com.ms.utils.mockpit.security;

import com.ms.utils.mockpit.aop.exception.MockpitApplicationException;
import com.ms.utils.mockpit.config.MockpitProperties;
import com.ms.utils.mockpit.domain.AppUser;
import com.ms.utils.mockpit.domain.Role;
import com.ms.utils.mockpit.repository.AppUserRepository;
import com.ms.utils.mockpit.web.dto.AuthDtos.SignupRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

/**
 * User-account business logic. Centralised so that signup validation, password lockout, and
 * password rotation rules live in one place.
 */
@Service
public class AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
    private static final Pattern EMAIL_REGEX = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockpitProperties properties;

    @Transactional
    public AppUser signup(SignupRequest req) throws MockpitApplicationException {
        if (req == null || req.email == null || req.password == null) {
            throw new MockpitApplicationException("Email and password are required.");
        }
        String email = req.email.trim().toLowerCase();
        if (!EMAIL_REGEX.matcher(email).matches()) {
            throw new MockpitApplicationException("Invalid email address.");
        }
        validatePasswordStrength(req.password);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            // Don't disclose user existence; return a generic success-shaped error.
            throw new MockpitApplicationException("Unable to create account with the supplied details.");
        }
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(req.password));
        user.setDisplayName(req.displayName == null ? email : req.displayName.trim());
        user.setRole(Role.USER);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    /**
     * Increment failed-login counter; lock the account for {@value #LOCKOUT_MINUTES} minutes once
     * {@value #MAX_FAILED_ATTEMPTS} consecutive failures are recorded.
     */
    @Transactional
    public void recordFailedLogin(String email) {
        userRepository.findByEmailIgnoreCase(email).ifPresent(u -> {
            int attempts = u.getFailedLoginAttempts() + 1;
            u.setFailedLoginAttempts(attempts);
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                u.setLockedUntil(Instant.now().plus(LOCKOUT_MINUTES, ChronoUnit.MINUTES));
                LOGGER.warn("User locked out after {} failed login attempts: {}", attempts, mask(email));
            }
            userRepository.save(u);
        });
    }

    @Transactional
    public void recordSuccessfulLogin(AppUser user) {
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
    }

    private void validatePasswordStrength(String password) throws MockpitApplicationException {
        if (password.length() < 12) {
            throw new MockpitApplicationException("Password must be at least 12 characters long.");
        }
        boolean hasLower = false, hasUpper = false, hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }
        if (!(hasLower && hasUpper && hasDigit)) {
            throw new MockpitApplicationException("Password must contain upper, lower case letters and a digit.");
        }
    }

    private static String mask(String email) {
        if (email == null || email.length() < 3) return "***";
        int at = email.indexOf('@');
        if (at <= 1) return "***" + email.substring(at);
        return email.charAt(0) + "***" + email.substring(at);
    }

    public MockpitProperties getProperties() {
        return properties;
    }
}
