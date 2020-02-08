package com.calbitica.app.Models.Calendars;

import java.util.List;

public class Calendars {
    // We want to create a list, so that it will become array at first started brackets...
    private List<Calendar> data;

    public List<Calendar> getData() {
        return data;
    }

    public void setData(List<Calendar> data) {
        this.data = data;
    }
}
