package com.ms.utils.mockpit.config;

import com.ms.utils.mockpit.service.LiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguration implements WebMvcConfigurer {

    private final Logger LOGGER = LoggerFactory.getLogger(CorsConfiguration.class);

    @Autowired
    private Environment env;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        LOGGER.info("Registering CORS configuration : {}, {}, {}", env.getProperty("spring.mvc.cors.allowed-origins"),
                env.getProperty("spring.mvc.cors.allowed-methods"),
                env.getProperty("spring.mvc.cors.allowed-headers"));
        registry.addMapping("/**")
                .allowedOrigins(env.getProperty("spring.mvc.cors.allowed-origins"))
                .allowedMethods(env.getProperty("spring.mvc.cors.allowed-methods"))
                .allowedHeaders(env.getProperty("spring.mvc.cors.allowed-headers"));
    }
}