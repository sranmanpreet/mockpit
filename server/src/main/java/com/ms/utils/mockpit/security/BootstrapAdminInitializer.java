package com.ms.utils.mockpit.security;

import com.ms.utils.mockpit.config.MockpitProperties;
import com.ms.utils.mockpit.domain.AppUser;
import com.ms.utils.mockpit.domain.Role;
import com.ms.utils.mockpit.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * On first startup with an empty user table, optionally seeds an admin account from the
 * {@code mockpit.security.bootstrap-admin.*} configuration. Disabled by default in {@code prod}
 * to force operators to think about credentials. The seeded password should be rotated immediately
 * after first login - the warning log line below makes that intent obvious.
 */
@Component
public class BootstrapAdminInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BootstrapAdminInitializer.class);

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MockpitProperties properties;

    public BootstrapAdminInitializer(AppUserRepository userRepository,
                                     PasswordEncoder passwordEncoder,
                                     MockpitProperties properties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        MockpitProperties.BootstrapAdmin cfg = properties.getSecurity().getBootstrapAdmin();
        if (!cfg.isEnabled()) {
            return;
        }
        if (userRepository.count() > 0) {
            return;
        }
        if (cfg.getEmail() == null || cfg.getEmail().isEmpty()
                || cfg.getPassword() == null || cfg.getPassword().length() < 12) {
            LOGGER.warn("Bootstrap admin enabled but email/password are missing or too weak; skipping seed.");
            return;
        }
        AppUser admin = new AppUser();
        admin.setEmail(cfg.getEmail().trim().toLowerCase());
        admin.setPasswordHash(passwordEncoder.encode(cfg.getPassword()));
        admin.setDisplayName("Admin");
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        admin.setEmailVerified(true);
        userRepository.save(admin);
        LOGGER.warn("Bootstrap admin account seeded: {} - rotate this password immediately.", admin.getEmail());
    }
}
