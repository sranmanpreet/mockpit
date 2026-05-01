package com.ms.utils.mockpit.web.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ms.utils.mockpit.config.MockpitProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Per-IP token-bucket rate limiter applied to every request. Two separate budgets:
 * the {@code admin} budget covers everything under {@code /native/} and {@code /auth/}; everything
 * else falls under the {@code live} budget. When a budget is exhausted we return
 * {@code 429 Too Many Requests} with a {@code Retry-After} header.
 *
 * <p>The implementation is in-process (Caffeine-backed). Bucket4j supports Hazelcast/Redis
 * back-ends if multi-instance enforcement is needed; we expose a TODO marker for that wiring.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 200)
public class RateLimitFilter extends OncePerRequestFilter {

    @Autowired
    private MockpitProperties properties;

    private final Cache<String, Bucket> adminBuckets = Caffeine.newBuilder()
            .maximumSize(50_000)
            .expireAfterAccess(Duration.ofMinutes(15))
            .build();

    private final Cache<String, Bucket> liveBuckets = Caffeine.newBuilder()
            .maximumSize(50_000)
            .expireAfterAccess(Duration.ofMinutes(15))
            .build();

    private final AtomicReference<Bandwidth> adminBandwidth = new AtomicReference<>();
    private final AtomicReference<Bandwidth> liveBandwidth = new AtomicReference<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        if (!properties.getRatelimit().isEnabled()) {
            chain.doFilter(request, response);
            return;
        }
        String key = clientIp(request);
        boolean isAdmin = isAdminPath(request.getRequestURI());
        Bucket bucket = isAdmin
                ? adminBuckets.get(key, k -> Bucket.builder().addLimit(adminBandwidth()).build())
                : liveBuckets.get(key, k -> Bucket.builder().addLimit(liveBandwidth()).build());

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
            return;
        }
        response.setStatus(429);
        response.setHeader("Retry-After", "60");
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"rate_limited\",\"message\":\"Too many requests.\"}");
    }

    private Bandwidth adminBandwidth() {
        Bandwidth b = adminBandwidth.get();
        if (b == null) {
            long quota = properties.getRatelimit().getAdminRequestsPerMinute();
            b = Bandwidth.builder()
                    .capacity(quota)
                    .refillIntervally(quota, Duration.ofMinutes(1))
                    .build();
            adminBandwidth.set(b);
        }
        return b;
    }

    private Bandwidth liveBandwidth() {
        Bandwidth b = liveBandwidth.get();
        if (b == null) {
            long quota = properties.getRatelimit().getLiveRequestsPerMinute();
            b = Bandwidth.builder()
                    .capacity(quota)
                    .refillIntervally(quota, Duration.ofMinutes(1))
                    .build();
            liveBandwidth.set(b);
        }
        return b;
    }

    private static boolean isAdminPath(String uri) {
        return uri != null && (uri.startsWith("/native/") || uri.startsWith("/auth/"));
    }

    /**
     * Pick the first IP in {@code X-Forwarded-For} (set by the reverse proxy) when present, else
     * the direct remote address. The forwarded header is sanitised - we only accept characters
     * valid in IP literals.
     */
    private static String clientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            String first = xff.split(",")[0].trim();
            if (first.matches("[0-9a-fA-F:.]+")) {
                return first;
            }
        }
        String remote = req.getRemoteAddr();
        return remote == null ? "unknown" : remote;
    }
}
