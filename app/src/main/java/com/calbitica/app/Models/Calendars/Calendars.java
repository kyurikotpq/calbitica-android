package com.calbitica.app.Models.Calendars;

import java.util.List;

public class Calendars {
    // We want to create a list, so that it will become array at first started brackets...
    private List<CalbiticaCalendar> data;

    public List<CalbiticaCalendar> getData() {
        return data;
    }

    public void setData(List<CalbiticaCalendar> data) {
        this.data = data;
    }
}
