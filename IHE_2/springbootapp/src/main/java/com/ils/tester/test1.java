/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ils.tester;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.log4j.Logger;

/**
 *
 * @author maverick
 */
public class test1 {

 static   Logger logger = Logger.getLogger(test1.class);

    public static void main123(String[] args) {

        Connection conn = null;
        try {
            final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
            final String DB_URL = "jdbc:mysql://172.20.10.3:3306/spring_boot_db";
            final String USER = "myuser";
            final String PASS = "Sachin#123";
//               logger.info(JDBC_DRIVER + " :: " + DB_URL + " :: " + USER + " :: " + PASS);
            logger.info("Connnection  Init " + java.time.LocalDateTime.now());
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            conn.setAutoCommit(false);
            logger.info("Connnection created successfully " + conn + " .. " + java.time.LocalDateTime.now());

        } catch (Exception e) {
            logger.info(" Error : : " + e + " :  " + java.time.LocalDateTime.now());
            try {
                conn.close();
            } catch (SQLException ex) {
                logger.info(" SQLException : " + ex + " :  " + java.time.LocalDateTime.now());
            }
            System.exit(0);

        }

    }
}
