package com.calbitica.app.Models.Calbit;

import retrofit2.Call;
import retrofit2.http.GET;

public interface CalbitInterface {
    // Get the Events from the Calbitica
    @GET("calbit")
    Call<Calbits> getAllCalbits();
}
