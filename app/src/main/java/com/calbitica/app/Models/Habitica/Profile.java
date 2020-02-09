package com.calbitica.app.Models.Habitica;

// This profile is part of the HabiticaProfileResponse.
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
