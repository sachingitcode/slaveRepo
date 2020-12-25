/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ils.config;

import com.ils.util.FileSystemStorageService;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 *
 * @author maverick
 */
@Configuration
@ComponentScan("com.ils")
@PropertySource("classpath:bootstrap.properties")
@EnableCaching
public class AppConfig {

//    @Value("${spring.datasource.url:}")
//    private String VURL;
//
//    @Value("${spring.datasource.user:}")
//    private String VUSER;
//
//    @Value("${spring.datasource.password:}")
//    private String VPASSWORD;
    @Autowired
    Environment environment;

    private final String URL = "spring.datasource.url";
    private final String USER = "spring.datasource.username";
    private final String PASSWORD = "spring.datasource.password";

    @Bean
    DataSource dataSource() {
        Logger logger = Logger.getLogger(AppConfig.class);

        DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
        driverManagerDataSource.setUrl(environment.getProperty(URL));
        driverManagerDataSource.setUsername(environment.getProperty(USER));
        driverManagerDataSource.setPassword(environment.getProperty(PASSWORD));
        logger.info(" URL  " + environment.getProperty(URL));
        logger.info(" USER  " + environment.getProperty(USER));
        return driverManagerDataSource;
    }
}
