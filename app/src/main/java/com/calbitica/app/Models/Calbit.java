package com.calbitica.app.Models;

import java.sql.Array;
import java.util.List;

import androidx.annotation.Nullable;

// Models ONE calbit
public class Calbit {
//    {
//        data: [
//            {
//                _id: "objectid",                          // MongoDB ID
//                reminders: "array",
//                summary: "string",                        // Title
//                start: [
//                    {
//                        date: "string"          or
//                        dateTime: "string"
//                    }
//                ],
//                end: [
//                    {
//                        date: "string"          or
//                        dateTime: "string"
//                    }
//                ],
//                isDump: "boolean",                        // true if brain dump or not assigned date time yet
//                googleID: "string",                       // Google Event ID, can be null - only exists on Habitica [not supported yet]
//                calendarID: "string",                     // MongoDB Calendar ID
//                completed: [
//                    {
//                        status: "boolean"
//                    }
//                ],
//                description: "null",
//                allDay: "boolean"
//            }
//        ]
//    }

    private Object _id;
    private Array reminders;
    private String summary;
    private List<StartDateTime> start;
    private List<EndDateTime> end;
    private Boolean isDump;
    private String googleID;
    private String calendarID;
    private List<TaskCompleted> completed;
    private Nullable description;
    private Boolean allDay;

    public Object get_id() {
        return _id;
    }

    public void set_id(Object _id) {
        this._id = _id;
    }

    public Array getReminders() {
        return reminders;
    }

    public void setReminders(Array reminders) {
        this.reminders = reminders;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<StartDateTime> getStart() {
        return start;
    }

    public void setStart(List<StartDateTime> start) {
        this.start = start;
    }

    public List<EndDateTime> getEnd() {
        return end;
    }

    public void setEnd(List<EndDateTime> end) {
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

    public List<TaskCompleted> getCompleted() {
        return completed;
    }

    public void setCompleted(List<TaskCompleted> completed) {
        this.completed = completed;
    }

    public Nullable getDescription() {
        return description;
    }

    public void setDescription(Nullable description) {
        this.description = description;
    }

    public Boolean getAllDay() {
        return allDay;
    }

    public void setAllDay(Boolean allDay) {
        this.allDay = allDay;
    }

    public Calbit(Object _id, Array reminders, String summary, List<StartDateTime> start, List<EndDateTime> end, Boolean isDump, String googleID, String calendarID, List<TaskCompleted> completed, Nullable description, Boolean allDay) {
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
        this.allDay = allDay;
    }
}