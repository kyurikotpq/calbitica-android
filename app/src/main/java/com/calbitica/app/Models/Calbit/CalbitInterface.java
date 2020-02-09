package com.calbitica.app.Models.Calbit;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
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

    // Edit the Event from Calbitica
    @PUT("calbit/{id}")
    Call<HashMap<String, Object>> updateCalbit(
            @Path("id") String id,
            @Body HashMap<String, String> calbit);

    // Delete the Event from Calbitica
    @DELETE("calbit/{id}")
    Call<Void> deleteCalbit(@Path("id") String id);

    // Update Calbit's Completion Status
    @PUT("calbit/{id}/complete")
    Call<HashMap<String, Object>> updateCalbitStatus(
            @Path("id") String id,
            @Body HashMap<String, Boolean> status);     // Declare the body same with the Call, then add-in necessary field for API into Calbit Class
}
