package com.ms.utils.mockpit.web;

import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MiscResource {

    @RequestMapping(value = "/native/**")
    public void handleNativeRoutes(){
        System.out.println("Handle native routes called");
    }
}
