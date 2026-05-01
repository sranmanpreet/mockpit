package com.ms.utils.mockpit.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.ms.utils.mockpit.auth.config.AuthConfig;
import com.ms.utils.mockpit.auth.config.AuthFailureResponse;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MockDTO {

    private Long id;
    private String name;
    private String description;
    private boolean inactive;
    private RouteDTO route;
    private List<ResponseHeaderDTO> responseHeaders;
    private ResponseBodyDTO responseBody;
    private ResponseStatusDTO responseStatus;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    /** Owner of the mock (read-only on the wire; ignored on writes). */
    private Long userId;

    /** Polymorphic per-mock auth configuration. {@code null} or {type:NONE} = no auth. */
    private AuthConfig authConfig;

    /**
     * Raw incoming authConfig payload. Jackson populates this directly so the controller can run
     * the polymorphic deserialiser through the codec (which performs encryption + hashing) instead
     * of relying on the default constructor for {@link AuthConfig}.
     */
    @com.fasterxml.jackson.annotation.JsonAlias("authConfig")
    private JsonNode authConfigRaw;

    private AuthFailureResponse authFailure;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getInactive() {
        return inactive;
    }

    public void setInactive(boolean inactive) {
        this.inactive = inactive;
    }

    public RouteDTO getRoute() {
        return route;
    }

    public void setRoute(RouteDTO route) {
        this.route = route;
    }

    public List<ResponseHeaderDTO> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(List<ResponseHeaderDTO> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public ResponseBodyDTO getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(ResponseBodyDTO responseBody) {
        this.responseBody = responseBody;
    }

    public ResponseStatusDTO getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(ResponseStatusDTO responseStatus) {
        this.responseStatus = responseStatus;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public AuthConfig getAuthConfig() { return authConfig; }
    public void setAuthConfig(AuthConfig authConfig) { this.authConfig = authConfig; }
    public JsonNode getAuthConfigRaw() { return authConfigRaw; }
    public void setAuthConfigRaw(JsonNode authConfigRaw) { this.authConfigRaw = authConfigRaw; }
    public AuthFailureResponse getAuthFailure() { return authFailure; }
    public void setAuthFailure(AuthFailureResponse authFailure) { this.authFailure = authFailure; }
}
