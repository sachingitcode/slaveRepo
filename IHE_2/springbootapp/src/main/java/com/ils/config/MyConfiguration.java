/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ils.config;

import com.ils.Interceptor.Interceptor;
import com.ils.common.controller.CommonJdbcUtil;
import com.ils.util.Common;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 *
 * @author maverick
 */
@Configuration
public class MyConfiguration implements WebMvcConfigurer {
    Logger logger = Logger.getLogger(MyConfiguration.class);
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
         logger.info("MyConfiguration addInterceptors ..  ");
        // LogInterceptor apply to all URLs.
        registry.addInterceptor(new Interceptor());

        // Old Login url, no longer use.
        // Use OldURLInterceptor to redirect to a new URL.
        //registry.addInterceptor(new OldLoginInterceptor())//
        //      .addPathPatterns("/admin/oldLogin");
        // This interceptor apply to URL like /admin/*
        // Exclude /admin/oldLogin
        //registry.addInterceptor(new AdminInterceptor())//
        //     .addPathPatterns("/admin/*")//
        //     .excludePathPatterns("/admin/oldLogin");
    }

    @Bean
    public CommonJdbcUtil commonJdbcUtil() {
        return new CommonJdbcUtil();
    }

    @Bean
    public Common common() {
        return new Common();
    }
}
