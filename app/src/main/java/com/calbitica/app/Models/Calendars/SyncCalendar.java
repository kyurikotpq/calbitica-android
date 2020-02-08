package com.calbitica.app.Models.Calendars;

public class SyncCalendar {
    // Create a list will become array
    // But, we want objects here, so will be just the another class
    private Calendar data;

    public Calendar getData() {
        return data;
    }

    public void setData(Calendar data) {
        this.data = data;
    }
}
