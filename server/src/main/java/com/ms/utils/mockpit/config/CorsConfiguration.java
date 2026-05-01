package com.ms.utils.mockpit.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * Allowlist-driven CORS configuration. The wildcard pre-2.0 behaviour has been removed because it
 * combines unsafely with the new cookie-based admin auth and would let any origin drive the API.
 *
 * <p>Use {@code MOCKPIT_ALLOWED_ORIGINS=https://app.example.com,https://other.example.com} to
 * configure allowed front-end origins. Origins are matched literally; no wildcards. The configured
 * value is logged at boot so misconfiguration is easy to spot.
 */
@Configuration
public class CorsConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(CorsConfiguration.class);

    @Autowired
    private MockpitProperties properties;

    @PostConstruct
    public void logEffectiveConfig() {
        MockpitProperties.Cors cors = properties.getCors();
        if (cors.getAllowedOrigins().isEmpty()) {
            LOGGER.warn("CORS allowedOrigins is empty - no cross-origin requests will be accepted.");
        } else if (cors.getAllowedOrigins().stream().anyMatch(o -> o.contains("*"))) {
            throw new IllegalStateException("CORS wildcards are not permitted. Configure mockpit.cors.allowed-origins"
                    + " with explicit scheme://host[:port] entries.");
        }
        LOGGER.info("CORS allowedOrigins={}", cors.getAllowedOrigins());
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        MockpitProperties.Cors c = properties.getCors();
        org.springframework.web.cors.CorsConfiguration cfg = new org.springframework.web.cors.CorsConfiguration();
        cfg.setAllowedOrigins(c.getAllowedOrigins());
        cfg.setAllowedMethods(orDefault(c.getAllowedMethods(),
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")));
        cfg.setAllowedHeaders(orDefault(c.getAllowedHeaders(),
                List.of("Authorization", "Content-Type", "X-Requested-With", "X-CSRF-TOKEN")));
        cfg.setExposedHeaders(c.getExposedHeaders());
        cfg.setAllowCredentials(c.isAllowCredentials());
        cfg.setMaxAge(c.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    private static List<String> orDefault(List<String> v, List<String> fallback) {
        return v == null || v.isEmpty() ? fallback : v;
    }
}
