package com.calbitica.app.Util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {
    static DateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public static String localToUTC(Date localDate) {
        isoDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String utcISOString = isoDateFormat.format(localDate);
        System.out.println(utcISOString);
        return utcISOString;
    }

    public static Date utcToLocalDate(String date) {
//        Date local = new Date(date.getTime() + TimeZone.getDefault().getOffset(date.getTime()));
//        return local;
        return new Date();
    }
}
