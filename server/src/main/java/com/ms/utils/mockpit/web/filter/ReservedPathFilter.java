package com.ms.utils.mockpit.web.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Prevents user-created mocks from shadowing reserved internal paths.
 *
 * <p>The {@code LiveResource} controller is mapped to {@code /**} so any URL not claimed by another
 * Spring MVC mapping reaches it and gets resolved as a mock. While Spring will prefer more specific
 * mappings (e.g. {@code /native/api/mocks}) over the {@code /**} pattern, a creative user could still
 * try to register a mock whose path collides with an admin / actuator / auth endpoint and rely on
 * future code paths or proxies. This filter short-circuits any inbound request whose URI begins with
 * a reserved prefix and is not handled by a registered controller, returning 404 instead of falling
 * through to the live mock dispatcher.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
public class ReservedPathFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservedPathFilter.class);

    private static final List<String> RESERVED_PREFIXES = Arrays.asList(
            "/native/",
            "/auth/",
            "/actuator/",
            "/swagger-ui",
            "/v3/api-docs",
            "/error"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        chain.doFilter(request, response);
    }

    /**
     * Returns true when {@code path} starts with one of the {@link #RESERVED_PREFIXES} so the
     * mock-dispatch service can reject it before performing a database lookup.
     */
    public static boolean isReservedPath(String path) {
        if (path == null) {
            return false;
        }
        for (String prefix : RESERVED_PREFIXES) {
            if (path.equals(prefix.replaceAll("/$", "")) || path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
