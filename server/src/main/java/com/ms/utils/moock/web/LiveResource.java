package com.ms.utils.moock.web;

import com.ms.utils.moock.dto.LiveResponseDTO;
import com.ms.utils.moock.service.LiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value="/**")
@Order(Ordered.LOWEST_PRECEDENCE)
public class LiveResource {

    @Autowired
    private LiveService liveResponseService;

    @RequestMapping
    public ResponseEntity<LiveResponseDTO> handleGetRequests(HttpServletRequest request){
        System.out.println("Handle Get Requests called");
        System.out.println(request.getRequestURI() + " " + request.getMethod());
        LiveResponseDTO liveResponse = liveResponseService.getLiveResponse(request.getRequestURI(), request.getMethod());
        return ResponseEntity.ok(liveResponse);
    }
}
