package com.calbitica.app.Models.Calbit;

import java.sql.Array;
import java.util.List;

// Models ONE calbit
public class Calbit {
//    {
//        data: [
//            {
//                _id: "objectid",                        // MongoDB ID
//                reminders: "array",
//                summary: string,                        // Title
//                start: {
//                      date: string          or
//                      dateTime: string
//                },
//                end: {
//                      date: string          or
//                      dateTime: string
//                },
//                isDump: boolean,                        // true if brain dump or not assigned date time yet
//                googleID: string,                       // Google Event ID, can be null - only exists on HabiticaProfileResponse [not supported yet]
//                calendarID: string,                     // MongoDB Calendar ID
//                completed: {
//                      status: boolean
//                },
//                location: string,
//                description: string,
//                legitAllDay: boolean,
//                allDay: boolean
//            }
//        ]
//    }

    private Object _id;
    private List<Array> reminders;
    private String summary;
    private StartDateTime start;
    private EndDateTime end;
    private Boolean isDump;
    private String googleID;
    private String calendarID;
    private TaskCompleted completed;
    private String location;
    private String description;
    private Boolean legitAllDay;
    private Boolean allDay;

    public Object get_id() {
        return _id;
    }

    public void set_id(Object _id) {
        this._id = _id;
    }

    public List<Array> getReminders() {
        return reminders;
    }

    public void setReminders(List<Array> reminders) {
        this.reminders = reminders;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public StartDateTime getStart() {
        return start;
    }

    public void setStart(StartDateTime start) {
        this.start = start;
    }

    public EndDateTime getEnd() {
        return end;
    }

    public void setEnd(EndDateTime end) {
        this.end = end;
    }

    public Boolean getDump() {
        return isDump;
    }

    public void setDump(Boolean dump) {
        isDump = dump;
    }

    public String getGoogleID() {
        return googleID;
    }

    public void setGoogleID(String googleID) {
        this.googleID = googleID;
    }

    public String getCalendarID() {
        return calendarID;
    }

    public void setCalendarID(String calendarID) {
        this.calendarID = calendarID;
    }

    public TaskCompleted getCompleted() {
        return completed;
    }

    public void setCompleted(TaskCompleted completed) {
        this.completed = completed;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getLegitAllDay() {
        return legitAllDay;
    }

    public void setLegitAllDay(Boolean legitAllDay) {
        this.legitAllDay = legitAllDay;
    }

    public Boolean getAllDay() {
        return allDay;
    }

    public void setAllDay(Boolean allDay) {
        this.allDay = allDay;
    }

    public Calbit(Object _id, List<Array> reminders, String summary, StartDateTime start, EndDateTime end, Boolean isDump, String googleID, String calendarID, TaskCompleted completed, String description, Boolean legitAllDay, Boolean allDay) {
        this._id = _id;
        this.reminders = reminders;
        this.summary = summary;
        this.start = start;
        this.end = end;
        this.isDump = isDump;
        this.googleID = googleID;
        this.calendarID = calendarID;
        this.completed = completed;
        this.description = description;
        this.legitAllDay = legitAllDay;
        this.allDay = allDay;
    }
}