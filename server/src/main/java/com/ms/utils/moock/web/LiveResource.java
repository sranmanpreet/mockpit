package com.ms.utils.moock.web;

import com.ms.utils.moock.dto.LiveResponseDTO;
import com.ms.utils.moock.service.LiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @RequestMapping("/**")
    public ResponseEntity<Object> handleLiveRequests(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("Request received by  LiveResource...");
        LOGGER.info(request.getRequestURI() + " " + request.getMethod());
        LOGGER.info("Nano time : {}", System.nanoTime());
        LiveResponseDTO liveResponse = liveResponseService.getLiveResponse(request.getRequestURI(), request.getMethod());
        if(Objects.isNull(liveResponse)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Oops! Resource not found.");
        }
        liveResponse.getHeaders().forEach(header -> response.addHeader(header.getName(), header.getValue()));

        return ResponseEntity.status(liveResponse.getStatusCode())
                .contentType(MediaType.parseMediaType(liveResponse.getContentType())).body(liveResponse.getBody());
    }
}
