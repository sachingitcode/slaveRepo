/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ils.security.model;

/**
 *
 * @author maverick
 */
public class AppUser {
    
    private Long userid;
    private String userName;
    private String encryptedPassword;

    public AppUser() {
    }

    public AppUser(Long userid, String userName, String encryptedPassword) {
        this.userid = userid;
        this.userName = userName;
        this.encryptedPassword = encryptedPassword;
    }

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }
    
    
}
