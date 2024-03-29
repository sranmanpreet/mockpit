package com.ms.utils.mockpit.web;

import com.ms.utils.mockpit.aop.exception.MockNotFoundException;
import com.ms.utils.mockpit.aop.interceptor.LogExecutionTime;
import com.ms.utils.mockpit.dto.LiveResponseDTO;
import com.ms.utils.mockpit.service.LiveService;
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

    @Autowired
    private LiveService liveResponseService;

    private final Logger LOGGER = LoggerFactory.getLogger(LiveResource.class);

    @LogExecutionTime
    @RequestMapping("/**")
    public ResponseEntity<Object> handleLiveRequests(HttpServletRequest request, HttpServletResponse response) throws MockNotFoundException {
        LOGGER.info("Request received by  LiveResource...");
        LOGGER.info(request.getRequestURI() + " " + request.getMethod());

        LiveResponseDTO liveResponse = liveResponseService.getLiveResponse(request, request.getMethod());
        if(Objects.isNull(liveResponse)){
            throw new MockNotFoundException("Resource not found");
        }
        liveResponseService.replaceContentTypeHeaderIfAny(liveResponse);
        liveResponse.getHeaders().forEach(header -> response.addHeader(header.getName(), header.getValue()));

        return ResponseEntity.status(liveResponse.getStatusCode())
                .contentType(MediaType.parseMediaType(liveResponse.getContentType()))
                .body(liveResponse.getBody());
    }
}
