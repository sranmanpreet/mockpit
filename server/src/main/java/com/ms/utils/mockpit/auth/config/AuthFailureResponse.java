package com.ms.utils.mockpit.auth.config;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Optional user-defined response that {@code MockAuthFilter} returns when authentication fails.
 * If absent the filter falls back to RFC-compliant defaults (401 + {@code WWW-Authenticate}).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthFailureResponse {
    private Integer status;
    private String body;
    private String contentType = "application/json";

    public AuthFailureResponse() { }
    public AuthFailureResponse(Integer status, String body, String contentType) {
        this.status = status;
        this.body = body;
        this.contentType = contentType;
    }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
}
