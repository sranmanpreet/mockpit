package com.ms.utils.mockpit.web.filter;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Tags every request/response with an {@code X-Request-Id} for log correlation. If the inbound
 * request already carries an {@code X-Request-Id} (e.g. set by an upstream proxy), it is reused;
 * otherwise a fresh UUID is generated. The id is also pushed into the SLF4J MDC under {@code requestId}
 * so the JSON logback encoder includes it on every log line for the duration of the request.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Request-Id";
    public static final String MDC_KEY = "requestId";
    private static final int MAX_LEN = 64;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String id = sanitise(request.getHeader(HEADER));
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        MDC.put(MDC_KEY, id);
        response.setHeader(HEADER, id);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    private static String sanitise(String v) {
        if (v == null || v.isEmpty()) return null;
        String trimmed = v.length() > MAX_LEN ? v.substring(0, MAX_LEN) : v;
        return trimmed.replaceAll("[^A-Za-z0-9._\\-]", "");
    }
}
