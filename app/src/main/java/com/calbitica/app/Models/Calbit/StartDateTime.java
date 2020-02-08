package com.calbitica.app.Models.Calbit;

import java.util.Date;

public class StartDateTime {
    private Date dateTime, date;
    private String timeZone;

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public StartDateTime(Date dateTime, Date date, String timeZone) {
        this.dateTime = dateTime;
        this.date = date;
        this.timeZone = timeZone;
    }
}
