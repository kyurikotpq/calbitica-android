package com.calbitica.app.Models;

import java.util.Date;

public class StartDateTime {
    private Date dateTime, date;

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

    public StartDateTime(Date dateTime, Date date) {
        this.dateTime = dateTime;
        this.date = date;
    }
}
