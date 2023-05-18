package com.ms.utils.moock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EntityScan("com.ms.utils.moock")
public class MoockApplication {

	public static void main(String[] args) {

		SpringApplication.run(MoockApplication.class, args);

	}

}
