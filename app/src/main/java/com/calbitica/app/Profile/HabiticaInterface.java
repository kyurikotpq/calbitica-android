package com.calbitica.app.Profile;

import retrofit2.Call;
import retrofit2.http.GET;

public interface HabiticaInterface {
    // Get the profile from the habiticaAPI
    @GET("profile")
    Call<Habitica> getHabiticaProfile();
}
