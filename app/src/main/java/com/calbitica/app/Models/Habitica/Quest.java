package com.calbitica.app.Models.Habitica;

public class Quest {
    private Progress progress;
    private Boolean RSVPNeeded;
    private String key, completed;

    public Progress getProgress() {
        return progress;
    }

    public void setProgress(Progress progress) {
        this.progress = progress;
    }

    public Boolean getRSVPNeeded() {
        return RSVPNeeded;
    }

    public void setRSVPNeeded(Boolean RSVPNeeded) {
        this.RSVPNeeded = RSVPNeeded;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCompleted() {
        return completed;
    }

    public void setCompleted(String completed) {
        this.completed = completed;
    }
}
