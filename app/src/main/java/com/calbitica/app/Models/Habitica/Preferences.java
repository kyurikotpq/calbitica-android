package com.calbitica.app.Models.Habitica;

public class Preferences {
    private Hair hair;
    private Tasks tasks;
    private String size, skin, shirt, chair, background;
    private Boolean sleep, disableClasses;

    public Hair getHair() {
        return hair;
    }

    public void setHair(Hair hair) {
        this.hair = hair;
    }

    public Tasks getTasks() {
        return tasks;
    }

    public void setTasks(Tasks tasks) {
        this.tasks = tasks;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getSkin() {
        return skin;
    }

    public void setSkin(String skin) {
        this.skin = skin;
    }

    public String getShirt() {
        return shirt;
    }

    public void setShirt(String shirt) {
        this.shirt = shirt;
    }

    public String getChair() {
        return chair;
    }

    public void setChair(String chair) {
        this.chair = chair;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public Boolean getSleep() {
        return sleep;
    }

    public void setSleep(Boolean sleep) {
        this.sleep = sleep;
    }

    public Boolean getDisableClasses() {
        return disableClasses;
    }

    public void setDisableClasses(Boolean disableClasses) {
        this.disableClasses = disableClasses;
    }
}
