/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ils.security.mapper;

import com.ils.config.MyConfiguration;
import com.ils.security.model.AppUser;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author maverick
 */
public class AppUserMapper implements RowMapper<AppUser> {

    public static final String BASE_SQL = "Select u.user_id, u.username, u.encrypted_password From USERS u ";
    Logger logger = Logger.getLogger(AppUserMapper.class);

    @Override
    public AppUser mapRow(ResultSet rs, int rowNum) throws SQLException {
        Long userId = rs.getLong("user_id");
        String userName = rs.getString("username");
        String encrytedPassword = rs.getString("encrypted_password");
        logger.info(" AppUserMapper :: " + encrytedPassword);
        return new AppUser(userId, userName, encrytedPassword);
    }

}
