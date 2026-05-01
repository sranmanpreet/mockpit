package com.ms.utils.mockpit.web;

import com.ms.utils.mockpit.aop.exception.MockNotFoundException;
import com.ms.utils.mockpit.aop.interceptor.LogExecutionTime;
import com.ms.utils.mockpit.dto.LiveResponseDTO;
import com.ms.utils.mockpit.service.LiveService;
import com.ms.utils.mockpit.web.filter.ReservedPathFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@RestController
@RequestMapping
public class LiveResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiveResource.class);

    @Autowired
    private LiveService liveResponseService;

    @LogExecutionTime
    @RequestMapping("/**")
    public ResponseEntity<Object> handleLiveRequests(HttpServletRequest request,
                                                     HttpServletResponse response) throws MockNotFoundException {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        if (ReservedPathFilter.isReservedPath(uri)) {
            throw new MockNotFoundException("Resource not found");
        }

        LOGGER.info("Live request: method={}, uri={}", sanitiseForLog(method), sanitiseForLog(uri));

        LiveResponseDTO liveResponse = liveResponseService.getLiveResponse(request, method);
        if (Objects.isNull(liveResponse)) {
            throw new MockNotFoundException("Resource not found");
        }
        liveResponseService.replaceContentTypeHeaderIfAny(liveResponse);
        liveResponse.getHeaders().forEach(header -> response.addHeader(header.getName(), header.getValue()));

        return ResponseEntity.status(liveResponse.getStatusCode())
                .contentType(MediaType.parseMediaType(liveResponse.getContentType()))
                .body(liveResponse.getBody());
    }

    /**
     * Strips CR/LF/null/control characters before any user-controlled value reaches the log appender.
     * Without this a crafted request URI like {@code /foo%0AINFO%20fake%20log%20line} could forge log
     * entries (CWE-117).
     */
    private static String sanitiseForLog(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.length() > 512 ? value.substring(0, 512) + "..." : value;
        return trimmed.replaceAll("[\\r\\n\\t\\u0000-\\u001F]", "_");
    }
}
