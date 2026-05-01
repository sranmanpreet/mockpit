package com.ms.utils.mockpit.aop.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Standardised error envelope returned by {@link RestExceptionHandler}.
 *
 * <p>The {@code debugMessage} field is only populated when running with the {@code dev} profile
 * (controlled via {@link RestExceptionHandler}); in any other profile it stays {@code null} so
 * detailed exception text never leaks to API clients (CWE-209).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private HttpStatus status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime timestamp;
    private String message;
    private String debugMessage;
    private List<ApiSubError> subErrors;

    private ApiError() {
        timestamp = LocalDateTime.now();
        subErrors = Collections.emptyList();
    }

    ApiError(HttpStatus status) {
        this();
        this.status = status;
        this.message = "";
    }

    ApiError(HttpStatus status, String message) {
        this();
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public void setStatus(HttpStatus status) { this.status = status; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getDebugMessage() { return debugMessage; }
    public void setDebugMessage(String debugMessage) { this.debugMessage = debugMessage; }
    public List<ApiSubError> getSubErrors() { return subErrors; }
    public void setSubErrors(List<ApiSubError> subErrors) { this.subErrors = subErrors; }
}
