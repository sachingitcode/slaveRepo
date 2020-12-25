/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ils.security.dao;

import com.ils.security.mapper.AppUserMapper;
import com.ils.security.model.AppUser;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author maverick
 */
@Repository
@Transactional
public class AppUserDAO extends JdbcDaoSupport {

    @Autowired
    public AppUserDAO(DataSource dataSource) {
        this.setDataSource(dataSource);
    }

    public AppUser findUserAccount(String userName) {

        String sql = AppUserMapper.BASE_SQL + " where u.username = ? ";
        Object[] params = new Object[]{userName};
        AppUserMapper mapper = new AppUserMapper();
        try {
            AppUser userInfo = this.getJdbcTemplate().queryForObject(sql, params, mapper);
             logger.info("findUserAccount  userInfo : "  + userInfo);
            return userInfo;
        } catch (EmptyResultDataAccessException e) {
             logger.info(" EmptyResultDataAccessException " + e);
            return null;
        }
    }
}
