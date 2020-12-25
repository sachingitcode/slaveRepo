/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ils.security.dao;

import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author maverick
 */

@Repository
@Transactional
public class AppRoleDAO extends JdbcDaoSupport{
    
    @Autowired
    public AppRoleDAO(DataSource dataSource) {
        this.setDataSource(dataSource);
    }
    
    public List<String> getRoleNames(Long userId) {
        String sql = "Select r.role_name  from USERS_ROLE ur, ROLES r where ur.role_id = r.role_id and ur.user_id = ? ";
        Object[] params = new Object[] { userId };
 
        List<String> roles = this.getJdbcTemplate().queryForList(sql, params, String.class);
         logger.info(" getRoleNames " +  roles);
        return roles;
    }
}
