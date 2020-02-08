package com.calbitica.app.Models.Calendars;

public class Reminder {
    private String method;
    private Integer minutes;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

    public Reminder(String method, Integer minutes) {
        this.method = method;
        this.minutes = minutes;
    }
}
