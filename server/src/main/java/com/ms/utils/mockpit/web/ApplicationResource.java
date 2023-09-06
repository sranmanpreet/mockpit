package com.ms.utils.mockpit.web;

import com.ms.utils.mockpit.config.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/native")
public class ApplicationResource {

    private final Logger LOGGER = LoggerFactory.getLogger(ApplicationResource.class);

    @Autowired
    private ApplicationProperties applicationProperties;

    @GetMapping("/app/properties")
    public ResponseEntity<ApplicationProperties> getApplicationProperties() {
        LOGGER.info("Reading application properties...");

        return new ResponseEntity<ApplicationProperties>(applicationProperties, HttpStatus.OK);
    }
}
