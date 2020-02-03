package com.calbitica.app.SyncCalendars;

import java.util.List;

public class Calendar {
//    {
//        data: [
//            {
//                _id: "objectid",
//                userID: "string",
//                googleID: "string",
//                summary: "string",
//                description: "string",
//                defaultReminders: [
//                    {
//                        method: "string",
//                        minutes: "integer"
//                    }
//                ],
//                sync: "boolean"
//            }
//        ]
//    }

    private Object _id;
    private String userID;
    private String googleID;
    private String summary;
    private String description;
    private Boolean sync;
    private List<Reminder> defaultReminders;

    public Object get_id() {
        return _id;
    }

    public void set_id(Object _id) {
        this._id = _id;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getGoogleID() {
        return googleID;
    }

    public void setGoogleID(String googleID) {
        this.googleID = googleID;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getSync() {
        return sync;
    }

    public void setSync(Boolean sync) {
        this.sync = sync;
    }

    public List<Reminder> getDefaultReminders() {
        return defaultReminders;
    }

    public void setDefaultReminders(List<Reminder> reminders) {
        this.defaultReminders = reminders;
    }

    public Calendar(Object _id, String userID, String googleID, String summary, String description, Boolean sync, List<Reminder> reminders) {
        this._id = _id;
        this.userID = userID;
        this.googleID = googleID;
        this.summary = summary;
        this.description = description;
        this.sync = sync;
        this.defaultReminders = reminders;
    }
}
