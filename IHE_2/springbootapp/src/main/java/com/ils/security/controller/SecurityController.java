/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ils.security.controller;

import com.ils.security.utils.WebUtils;
import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author maverick
 */
@Controller
public class SecurityController {

    Logger logger = Logger.getLogger(SecurityController.class);

    @RequestMapping(value = {"/login"}, method = RequestMethod.GET)
    public String welcomePage(Model model, Principal principal) {
        logger.info("welcomePage  Started");
        model.addAttribute("title", "Welcome");
        model.addAttribute("message", "This is welcome page!");
//         logger.info(" accessDenied  userInfo :" + userInfo);
//        User loginedUser = (User) ((Authentication) principal).getPrincipal();
//        String userInfo = WebUtils.toString(loginedUser);
//        model.addAttribute("userInfo", userInfo);
//        logger.info(" accessDenied  userInfo :" + userInfo);
//       logger.info( "Hi " + principal.getName());
        return "/pages/login";
    }

    @GetMapping(value = {"/logoutSuccessful"})
    public String logoutPage(HttpServletRequest request, HttpServletResponse response) {
        logger.info("loggg   Started");
        request.getSession().invalidate();
//        response.sendRedirect(request.getContextPath() + "/LoginPage.html");
        return "/pages/logout";
    }

    @RequestMapping(value = "/403", method = RequestMethod.GET)
    public String accessDenied(Model model, Principal principal) {

        if (principal != null) {
            User loginedUser = (User) ((Authentication) principal).getPrincipal();

            String userInfo = WebUtils.toString(loginedUser);

            model.addAttribute("userInfo", userInfo);
            logger.info(" accessDenied  userInfo :" + userInfo);
            String message = "Hi " + principal.getName() //
                    + "<br> You do not have permission to access this page!";
            model.addAttribute("message", message);
            logger.info("accessDenied  with 403 ");
        }

        return "/pages/403Page";
    }

    @GetMapping("/home")  //  addition add
    public String homePage(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        logger.info(" uNAME " + principal.getName());
        HttpSession session = request.getSession(true);
        session.setAttribute("userName", principal.getName());
        return "pages/homePage";
    }
    
    
    
}
