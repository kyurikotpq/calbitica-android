package com.calbitica.app.Models.Habitica;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.GET;

public interface HabiticaInterface {
    // Get the profile from the habiticaAPI
    @GET("profile")
    Call<HabiticaProfileResponse> getHabiticaProfile();

    // Toggle sleep status
    @GET("sleep")
    Call<HabiticaToggleSleepResponse> toggleSleep();
}
