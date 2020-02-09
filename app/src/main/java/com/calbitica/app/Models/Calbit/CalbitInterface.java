package com.calbitica.app.Models.Calbit;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CalbitInterface {
    // Get the Events from the Calbitica
    @GET("calbit")
    Call<Calbits> getAllCalbits();

    // Create the Event to Calbitica
    @POST("calbit")
    Call<HashMap<String, Object>> createCalbit(
            @Body HashMap<String, String> calbit
    );

    // Edit the Event to Calbitica
    @PUT("calbit/{id}")
    Call<Calbit> editCalbit(
            @Path("id") String id,
            @Body Calbit putBody);
}
