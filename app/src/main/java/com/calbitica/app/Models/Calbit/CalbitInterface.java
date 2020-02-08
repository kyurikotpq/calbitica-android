package com.calbitica.app.Models.Calbit;

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
    Call<Calbits> createCalbit();

    // Edit the Event from Calbitica
    @PUT("calbit/{id}")
    Call<Calbit> editCalbit(
            @Path("id") String id,
            @Body Calbit putBody);

    // Delete the Event from Calbitica
    @DELETE("calbit/{id}")
    Call<Void> deleteCalbit(@Path("id") String id);

    // Update Calbit's Completion Status
    @PUT("calbit/{id}/complete")
    Call<Boolean> updateCalbitStatus(
            @Path("id") String id,
            @Body Boolean status);
}
