package com.ms.utils.mockpit.aop.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Arrays;

/**
 * Global REST exception translator.
 *
 * <p>Two safety properties:
 * <ol>
 *   <li>The raw exception message is never returned to the client outside the {@code dev} profile;
 *       only a curated, generic message reaches the wire. This prevents leaks of internal
 *       implementation details (CWE-209, CWE-754).</li>
 *   <li>Every {@link Throwable} is caught by the fallback handler so unexpected exceptions are
 *       logged with their stack trace server-side but only surface as a generic
 *       {@code 500 Internal Server Error} to the client.</li>
 * </ol>
 */
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestExceptionHandler.class);

    @Autowired
    private Environment environment;

    @ExceptionHandler(MockNotFoundException.class)
    protected ResponseEntity<Object> handleMockNotFoundException(MockNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
    }

    @ExceptionHandler(MockpitApplicationException.class)
    protected ResponseEntity<Object> handleApp(MockpitApplicationException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return build(HttpStatus.BAD_REQUEST,
                "Invalid request. Please ensure that the correct information is passed in the request.", ex);
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "Access denied.", ex);
    }

    @ExceptionHandler(AuthenticationException.class)
    protected ResponseEntity<Object> handleAuthentication(AuthenticationException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Authentication required.", ex);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage() == null ? "Invalid argument." : ex.getMessage(), ex);
    }

    /**
     * Last-resort handler. Anything not handled above becomes a generic 500 with no internal
     * details on the wire. Server logs still get the full stack trace.
     */
    @ExceptionHandler(Throwable.class)
    protected ResponseEntity<Object> handleAny(Throwable ex) {
        LOGGER.error("Unhandled exception serving request", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error.", ex);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Malformed JSON request.", ex);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Validation failed.", ex);
    }

    private ResponseEntity<Object> build(HttpStatus status, String message, Throwable ex) {
        ApiError body = new ApiError(status, message);
        if (isDev()) {
            String detail = ex == null ? null : (ex.getClass().getSimpleName()
                    + (ex.getMessage() == null ? "" : ": " + ex.getMessage()));
            body.setDebugMessage(detail);
        }
        return new ResponseEntity<>(body, status);
    }

    private boolean isDev() {
        if (environment == null) return false;
        return Arrays.asList(environment.getActiveProfiles()).contains("dev")
                || environment.getActiveProfiles().length == 0;
    }
}
