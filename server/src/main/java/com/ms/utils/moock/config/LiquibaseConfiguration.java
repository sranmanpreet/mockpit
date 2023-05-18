package com.ms.utils.moock.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Configuration
public class LiquibaseConfiguration {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Environment environment;

    @Bean
    public SpringLiquibase liquibase() {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(environment.getProperty("spring.liquibase.change-log"));
        liquibase.setShouldRun(true);
        return liquibase;
    }
}

