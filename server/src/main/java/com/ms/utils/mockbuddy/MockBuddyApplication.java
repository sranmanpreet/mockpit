package com.ms.utils.mockbuddy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EntityScan("com.ms.utils.mockbuddy")
public class MockBuddyApplication {

	public static void main(String[] args) {

		SpringApplication.run(MockBuddyApplication.class, args);

	}

}
