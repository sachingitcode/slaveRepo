/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ils.common.controller;

import com.ils.util.Common;
import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 *
 * @author maverick
 */
@Controller
public class LoginController {

    Logger logger = Logger.getLogger(MainController.class);

    @Autowired
    private CommonJdbcUtil commonJdbcUtil;

    @Autowired
    private Common common;

    
    
      @GetMapping("/NewUserDetails")  //  addition add
    public String newuserController(HttpServletRequest request) {
        return "pages/newUserPage";
    }
    
    
    
    
    
}
