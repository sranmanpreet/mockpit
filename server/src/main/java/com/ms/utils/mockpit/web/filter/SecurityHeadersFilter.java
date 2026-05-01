package com.ms.utils.mockpit.web.filter;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Adds the baseline OWASP-recommended security headers to every response. These are deliberately
 * conservative defaults; loosen via reverse-proxy config if a particular deployment needs it.
 *
 * <p>For documents loaded into a browser:
 * <ul>
 *   <li>{@code Strict-Transport-Security}: enforce HTTPS for one year (skipped on plain-HTTP requests
 *       so dev mode still works).</li>
 *   <li>{@code Content-Security-Policy}: deny everything by default; the SPA only loads its own
 *       static assets and talks to its own origin.</li>
 *   <li>{@code X-Frame-Options: DENY} + CSP {@code frame-ancestors 'none'}: clickjacking defence.</li>
 *   <li>{@code X-Content-Type-Options: nosniff}: stops MIME-type confusion.</li>
 *   <li>{@code Referrer-Policy: no-referrer}: don't leak URLs that may include mock IDs.</li>
 *   <li>{@code Permissions-Policy}: deny camera/mic/geo by default.</li>
 *   <li>{@code Cache-Control: no-store} on API responses to keep auth cookies out of browser caches.
 *       (Static assets are served by nginx and get their own cache headers there.)</li>
 * </ul>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class SecurityHeadersFilter extends OncePerRequestFilter {

    private static final String CSP =
            "default-src 'self'; "
            + "script-src 'self'; "
            + "style-src 'self' 'unsafe-inline'; "
            + "img-src 'self' data:; "
            + "font-src 'self' data:; "
            + "connect-src 'self'; "
            + "frame-ancestors 'none'; "
            + "base-uri 'self'; "
            + "form-action 'self'";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        if (request.isSecure()) {
            response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        }
        response.setHeader("Content-Security-Policy", CSP);
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=(), interest-cohort=()");
        response.setHeader("Cross-Origin-Opener-Policy", "same-origin");
        response.setHeader("Cross-Origin-Resource-Policy", "same-site");

        String path = request.getRequestURI();
        if (path != null && (path.startsWith("/native/") || path.startsWith("/auth/"))) {
            response.setHeader("Cache-Control", "no-store");
            response.setHeader("Pragma", "no-cache");
        }

        chain.doFilter(request, response);
    }
}
