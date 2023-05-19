package com.ms.utils.moock.config;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ExcludePathInterceptor extends HandlerInterceptorAdapter {

    private static final String EXCLUDED_PATTERN = "/swagger-ui/**"; // Replace with your desired excluded pattern

    private final PathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = getRequestUri(request);
        if (isExcludedPath(requestUri)) {
            return true; // Allow the request to be handled by the excluded handler
        }

        // Your custom logic for handling other requests
        // ...

        return super.preHandle(request, response, handler);
    }

    private String getRequestUri(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && contextPath.length() > 0) {
            return requestUri.substring(contextPath.length());
        }
        return requestUri;
    }

    private boolean isExcludedPath(String requestUri) {
        return pathMatcher.match(EXCLUDED_PATTERN, requestUri);
    }
}
