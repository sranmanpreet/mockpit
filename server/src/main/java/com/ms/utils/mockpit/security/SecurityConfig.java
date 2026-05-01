package com.ms.utils.mockpit.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security configuration.
 *
 * <p>Two complementary chains:
 * <ul>
 *   <li>The <em>admin chain</em> covers {@code /native/**}, {@code /auth/**} and the actuator endpoints.
 *       It is stateless (JWT-in-cookie), CSRF-protected, and uses a custom 401 entry point so that
 *       unauthenticated requests get a clean JSON error rather than a redirect.</li>
 *   <li>The <em>live chain</em> covers everything else - i.e. the dynamically-served mock endpoints.
 *       It is fully permitAll because per-mock authentication is enforced by {@code MockAuthFilter}
 *       which runs <em>inside</em> the dispatcher; that filter has access to the resolved mock and
 *       can apply the user-configured scheme.</li>
 * </ul>
 */
@Configuration
public class SecurityConfig {

    @Autowired
    private JwtCookieAuthenticationFilter jwtCookieFilter;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    /**
     * Endpoints that never require a logged-in user.
     */
    private RequestMatcher publicMatchers() {
        return new OrRequestMatcher(
                new AntPathRequestMatcher("/auth/signup"),
                new AntPathRequestMatcher("/auth/login"),
                new AntPathRequestMatcher("/auth/logout"),
                new AntPathRequestMatcher("/auth/password-reset/**"),
                new AntPathRequestMatcher("/auth/csrf"),
                new AntPathRequestMatcher("/native/app/properties"),
                new AntPathRequestMatcher("/actuator/health"),
                new AntPathRequestMatcher("/actuator/info"),
                new AntPathRequestMatcher("/v3/api-docs/**"),
                new AntPathRequestMatcher("/swagger-ui/**"),
                new AntPathRequestMatcher("/swagger-ui.html"),
                new AntPathRequestMatcher("/error")
        );
    }

    @Bean
    public SecurityFilterChain adminChain(HttpSecurity http) throws Exception {
        // Restrict this chain to the admin/auth surface so the LiveResource catch-all is handled
        // by the permissive chain below.
        http.securityMatcher(new OrRequestMatcher(
                new AntPathRequestMatcher("/native/**"),
                new AntPathRequestMatcher("/auth/**"),
                new AntPathRequestMatcher("/actuator/**"),
                new AntPathRequestMatcher("/v3/api-docs/**"),
                new AntPathRequestMatcher("/swagger-ui/**"),
                new AntPathRequestMatcher("/swagger-ui.html")
        ));

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> {
                    // Spring Security 6 defaults to XorCsrfTokenRequestAttributeHandler which would
                    // break Angular's HttpClientXsrfModule (it expects the cookie value to match the
                    // header value verbatim). Use the plain handler instead.
                    CsrfTokenRequestAttributeHandler handler = new CsrfTokenRequestAttributeHandler();
                    handler.setCsrfRequestAttributeName(null);
                    csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(handler)
                        .ignoringRequestMatchers(
                                new AntPathRequestMatcher("/auth/login"),
                                new AntPathRequestMatcher("/auth/signup"),
                                new AntPathRequestMatcher("/auth/password-reset/**"));
                })
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(jwtCookieFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(publicMatchers()).permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/prometheus").hasRole("ADMIN")
                        .requestMatchers("/native/**").authenticated()
                        .requestMatchers("/auth/me", "/auth/refresh").authenticated()
                        .anyRequest().authenticated());

        return http.build();
    }

    @Bean
    public SecurityFilterChain liveChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
