/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ils.config;
 
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.apache.log4j.Logger;
import org.springframework.web.WebApplicationInitializer;

/**
 *
 * @author maverick
 */
public class MainWebAppInitializer implements WebApplicationInitializer {

    Logger logger = Logger.getLogger(MainWebAppInitializer.class);

    @Override
    public void onStartup(ServletContext sc) throws ServletException {
        logger.info(" MainWebAppInitializer onStartup .. ");
        
        sc.getSessionCookieConfig().setHttpOnly(true);
        sc.getSessionCookieConfig().setSecure(true);
        logger.info(" Session " + sc);
    }

}
