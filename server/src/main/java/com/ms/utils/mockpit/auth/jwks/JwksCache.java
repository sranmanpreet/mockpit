package com.ms.utils.mockpit.auth.jwks;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ms.utils.mockpit.auth.http.SafeHttpClient;
import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.time.Duration;

/**
 * Caches parsed {@link JWKSet} payloads keyed by JWKS URI. The cache is small and short-lived so
 * that key rotation by upstream providers takes effect within ten minutes; the {@link SafeHttpClient}
 * SSRF guard runs on every cache miss.
 */
@Component
public class JwksCache {

    private static final long MAX_CACHE_ENTRIES = 64;

    @Autowired
    private SafeHttpClient httpClient;

    private final Cache<String, JWKSet> cache = Caffeine.newBuilder()
            .maximumSize(MAX_CACHE_ENTRIES)
            .expireAfterWrite(Duration.ofMinutes(10))
            .build();

    public JWKSet get(String jwksUri) throws IOException, ParseException {
        JWKSet cached = cache.getIfPresent(jwksUri);
        if (cached != null) return cached;
        SafeHttpClient.Response resp = httpClient.getJson(jwksUri);
        if (resp.status / 100 != 2) {
            throw new IOException("JWKS endpoint returned HTTP " + resp.status);
        }
        JWKSet parsed = JWKSet.parse(resp.body);
        cache.put(jwksUri, parsed);
        return parsed;
    }
}
