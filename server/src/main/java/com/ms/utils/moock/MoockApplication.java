package com.ms.utils.moock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

@SpringBootApplication
@EnableJpaAuditing
@EntityScan("com.ms.utils.moock")
public class MoockApplication {

	public static void main(String[] args) {

		SpringApplication.run(MoockApplication.class, args);

	}

}
