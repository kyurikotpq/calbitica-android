package com.calbitica.app.Models.Habitica;

public class Profile {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Profile(String name) {
        this.name = name;
    }
}
