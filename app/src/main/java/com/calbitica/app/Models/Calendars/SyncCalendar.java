package com.calbitica.app.Models.Calendars;

public class SyncCalendar {
    // Create a list will become array
    // But, we want objects here, so will be just the another class
    private CalbiticaCalendar data;

    public CalbiticaCalendar getData() {
        return data;
    }

    public void setData(CalbiticaCalendar data) {
        this.data = data;
    }
}
