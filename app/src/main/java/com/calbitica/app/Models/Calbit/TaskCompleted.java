package com.calbitica.app.Models.Calbit;

public class TaskCompleted {
    private Boolean status;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public TaskCompleted(Boolean status) {
        this.status = status;
    }
}
