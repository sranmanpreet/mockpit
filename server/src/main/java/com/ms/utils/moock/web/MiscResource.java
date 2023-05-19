package com.ms.utils.moock.web;

import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Order(1000)
public class MiscResource {

    @RequestMapping(value = "/native/**")
    public void handleNativeRoutes(){
        System.out.println("Handle native routes called");
    }
}
