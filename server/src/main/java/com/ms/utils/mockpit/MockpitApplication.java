package com.ms.utils.mockpit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EntityScan("com.ms.utils.mockpit")
@ConfigurationPropertiesScan("com.ms.utils.mockpit")
public class MockpitApplication {

	public static void main(String[] args) {
		SpringApplication.run(MockpitApplication.class, args);
	}

}
