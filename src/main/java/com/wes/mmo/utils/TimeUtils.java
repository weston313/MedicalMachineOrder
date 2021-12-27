package com.wes.mmo.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TimeUtils {

    private static Map<String, SimpleDateFormat> SDFS = new HashMap<>();

    static {
        SDFS.put("yyyy-MM-dd-HH-mm",  new SimpleDateFormat("yyyy-MM-dd-HH-mm"));
        SDFS.put("yyyyMMdd", new SimpleDateFormat("yyyyMMdd"));
        SDFS.put("yyyyMMddHHmm", new SimpleDateFormat("yyyyMMddHHmm"));
        SDFS.put("yyyy-MM-dd HH:mm:ss", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }


    public static long ParseDateString(String value, String format){
        long timestamp = -1;
        try{
            if(SDFS.containsKey(format)){
                timestamp = SDFS.get(format).parse(value).getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        finally {
            return timestamp;
        }
    }

    public static String FormatDate(Date date, String format){
        String timestamp = "-";
        if(SDFS.containsKey(format)){
            timestamp = SDFS.get(format).format(date);
        }
        return timestamp;
    }

    public static Date getLastWeekSunday(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 ;
        calendar.add(Calendar.DAY_OF_MONTH, -1 * dayOfWeek);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    public static Date getSaturday(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 ;
        calendar.add(Calendar.DAY_OF_MONTH, (6 - dayOfWeek));
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    public static Date getNextSaturday(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 ;
        calendar.add(Calendar.DAY_OF_MONTH, 13 - dayOfWeek);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    public static int getDayOfWeek(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 ;
        return dayOfWeek;
    }


    public static Date getToday(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }


}
