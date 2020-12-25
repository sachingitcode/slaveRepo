/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ils.security.utils;

import org.apache.log4j.Logger;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 *
 * @author maverick
 */
public class EncryptedPasswordUtils {
 static  Logger  logger = Logger.getLogger( EncryptedPasswordUtils.class);

     // Encryte Password with BCryptPasswordEncoder
     public static String encrytePassword1(String password) {
          BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
          logger.info("It is EncryptedPassword  " + encoder.encode(password));
          return encoder.encode(password);
     }
}
