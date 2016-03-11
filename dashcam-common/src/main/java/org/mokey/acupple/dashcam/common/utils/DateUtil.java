package org.mokey.acupple.dashcam.common.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Yuan on 2015/7/30.
 */
public class DateUtil {
    public static final String DATE_FULL_STR = "yyyy-MM-dd HH:mm:ss";

    public static String getYear(Date date) {
        DateFormat f_year = new SimpleDateFormat("yyyy");
        return f_year.format(date).toString();
    }

    public static String getMonth(Date date) {
        DateFormat f_month = new SimpleDateFormat("MM");
        return f_month.format(date).toString();
    }

    public static String getDay(Date date) {
        DateFormat f_day = new SimpleDateFormat("dd");
        return f_day.format(date).toString();
    }

    public static int getHour(Date date){
        DateFormat f_hour = new SimpleDateFormat("HH");
        return Integer.parseInt(f_hour.format(date));
    }

    public static String getWeek(Date date) {
        DateFormat f_week = new SimpleDateFormat("EEEEEEE");
        return f_week.format(date).toString();
    }

    public static String getTime(Date date) {
        DateFormat f_time = new SimpleDateFormat("HH时mm分 ss秒");
        return f_time.format(date).toString();
    }

    public static Date getNextDay(Date date, int day){
        long dd = day;
        return new Date(date.getTime() + dd * 24*3600*1000);
    }

    public static Date parse(String strDate) {
        return parse(strDate, DATE_FULL_STR);
    }

    public static Date parse(String strDate, String pattern) {
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        try {
            return df.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Integer getRelativeDay(long time) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(time);
        return 365 - calendar.get(Calendar.DAY_OF_YEAR);
    }

    public static Integer getRelativeMillSeconds(long time) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(time);
        return Integer.valueOf(3600 * 24 * 1000 - (calendar
                .get(Calendar.HOUR_OF_DAY)
                * 3600
                * 1000
                + calendar.get(Calendar.MINUTE)
                * 60
                * 1000
                + calendar.get(Calendar.SECOND) * 1000 + calendar
                .get(Calendar.MILLISECOND)));
    }

    public static long getMinutePart(long timestamp) {
        return timestamp / (60 * 1000) * (60 * 1000);
    }

    public static long getHourPart(long timestamp) {
        return timestamp / (60 * 60 * 1000) * (60 * 60 * 1000);
    }

    public static long getNextHourPart(long timestamp) {
        return getHourPart(timestamp) + 60 * 60 * 1000;
    }
}
