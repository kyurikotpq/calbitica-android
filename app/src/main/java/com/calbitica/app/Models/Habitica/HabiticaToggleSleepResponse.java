package com.calbitica.app.Models.Habitica;

import java.util.HashMap;

public class HabiticaToggleSleepResponse {
    private String jwt;
    private HashMap<String, Object> data;

    public String getJwt() { return jwt; }

    public void setJwt(String jwt) { this.jwt = jwt; }

    public HashMap<String, Object>  getData() {
        return data;
    }

    public void setData(HashMap<String, Object>  data) {
        this.data = data;
    }
}

