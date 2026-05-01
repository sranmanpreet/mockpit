package com.ms.utils.mockpit.security;

import com.ms.utils.mockpit.config.MockpitProperties;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * Reads the {@code mockpit_session} cookie on every request, validates the JWT, and populates the
 * Spring Security context with an {@link UsernamePasswordAuthenticationToken}. If the token is
 * absent or invalid, the security chain falls through and unauthenticated handlers (or the
 * {@code AccessDeniedHandler}) take over.
 */
@Component
public class JwtCookieAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtCookieAuthenticationFilter.class);

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private MockpitProperties properties;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = readCookie(request, properties.getSecurity().getJwt().getCookieName());
            if (token != null) {
                try {
                    JwtTokenService.ParsedToken parsed = jwtTokenService.parse(token);
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            new JwtPrincipal(parsed.userId, parsed.email, parsed.role),
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + parsed.role))
                    );
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (JwtException ex) {
                    LOGGER.debug("Rejecting invalid JWT cookie: {}", ex.getClass().getSimpleName());
                }
            }
        }

        chain.doFilter(request, response);
    }

    private static String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }
}
