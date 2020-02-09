package com.calbitica.app.Util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {
    static DateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    static DateFormat isoDateFormatNoTime = new SimpleDateFormat("yyyy-MM-dd");
    static DateFormat ddMMMyyyyFormat = new SimpleDateFormat("dd MMM yyyy");
    static DateFormat HHmmFormat = new SimpleDateFormat("HH:mm");

    public static String localToUTC(Date localDate) {
        isoDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String utcISOString = isoDateFormat.format(localDate);
        return utcISOString;
    }

    public static String localToUTCAllDay(Date localDate) {
        isoDateFormatNoTime.setTimeZone(TimeZone.getTimeZone("UTC"));
        String utcISOString = isoDateFormatNoTime.format(localDate);
        return utcISOString;
    }

    public static String ddMMMyyyy(Date date) {
        return ddMMMyyyyFormat.format(date);
    }

    public static String HHmm(Date date) {
        return HHmmFormat.format(date);
    }

    public static Date utcToLocalDate(String date) {
//        Date local = new Date(date.getTime() + TimeZone.getDefault().getOffset(date.getTime()));
//        return local;
        return new Date();
    }
}
