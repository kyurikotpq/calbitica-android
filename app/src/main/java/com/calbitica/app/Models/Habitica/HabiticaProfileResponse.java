package com.calbitica.app.Models.Habitica;

public class HabiticaProfileResponse {
    // Create a list will become array
    // But, we want objects here, so will be just the another class.
    private HabiticaInfo data;

    public HabiticaInfo getData() {
        return data;
    }

    public void setData(HabiticaInfo data) {
        this.data = data;
    }
}