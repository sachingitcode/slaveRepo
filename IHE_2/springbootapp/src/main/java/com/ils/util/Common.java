/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ils.util;

import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author 
 */
public class Common {

    public String getFileExtension(String filename) {
        return FilenameUtils.getExtension(filename);
    }

    public String getAge(String date) {
        String[] date1 = date.split("-");
        LocalDate pdate = LocalDate.of(Integer.parseInt(date1[0]), Integer.parseInt(date1[1]), Integer.parseInt(date1[2]));
        int currentyear = Calendar.getInstance().get(Calendar.YEAR);
        LocalDate now = LocalDate.of(currentyear, 10, 01);

        Period diff = Period.between(pdate, now);
        String age = diff.getYears() + "#" + diff.getMonths() + "#" + diff.getDays();
        return age;
    }

}
