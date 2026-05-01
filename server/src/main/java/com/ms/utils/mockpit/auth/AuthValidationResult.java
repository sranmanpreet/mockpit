package com.ms.utils.mockpit.auth;

import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Outcome of an {@link AuthValidator} invocation. Carries either a success marker or the diagnostic
 * data needed to build a useful failure response (status code, {@code WWW-Authenticate}
 * challenge, error message).
 */
public final class AuthValidationResult {

    public enum Outcome { SUCCESS, FAILURE }

    private final Outcome outcome;
    private final HttpStatus suggestedStatus;
    private final String wwwAuthenticate;
    private final String reason;
    private final Map<String, Object> claims;

    private AuthValidationResult(Outcome outcome, HttpStatus status, String wwwAuthenticate,
                                 String reason, Map<String, Object> claims) {
        this.outcome = outcome;
        this.suggestedStatus = status;
        this.wwwAuthenticate = wwwAuthenticate;
        this.reason = reason;
        this.claims = claims == null ? Collections.emptyMap() : new HashMap<>(claims);
    }

    public static AuthValidationResult success() {
        return new AuthValidationResult(Outcome.SUCCESS, null, null, null, null);
    }

    public static AuthValidationResult success(Map<String, Object> claims) {
        return new AuthValidationResult(Outcome.SUCCESS, null, null, null, claims);
    }

    public static AuthValidationResult failure(HttpStatus status, String wwwAuthenticate, String reason) {
        return new AuthValidationResult(Outcome.FAILURE, status, wwwAuthenticate, reason, null);
    }

    public boolean isSuccess() { return outcome == Outcome.SUCCESS; }
    public boolean isFailure() { return outcome == Outcome.FAILURE; }
    public HttpStatus getSuggestedStatus() { return suggestedStatus; }
    public String getWwwAuthenticate() { return wwwAuthenticate; }
    public String getReason() { return reason; }
    public Map<String, Object> getClaims() { return claims; }
}
