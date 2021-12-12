package com.wes.mmo.utils;

import net.bytebuddy.build.Plugin;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.*;

public class TimeUtilsTest {

    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    @Test
    public void testGetLastWeekSunday() {
        Date orderDate = new Date(1639621567000l);
        System.out.println(sdf.format(TimeUtils.getLastWeekSunday(orderDate)));
    }

    @Test
    public void testGetSaturday() {
        Date orderDate = new Date(1639621567000l);
        System.out.println(sdf.format(TimeUtils.getSaturday(orderDate)));
    }

    @Test
    public void testGetNextSaturday() {
        Date orderDate = new Date(1639621567000l);
        System.out.println(sdf.format(TimeUtils.getNextSaturday(orderDate)));
    }

}