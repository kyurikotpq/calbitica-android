package com.calbitica.app.Models;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface CalbitInterface {
    // Get the Events from the Calbitica
    @GET("calbit")
    Call<Calbits> getAllCalbits();

    @POST("")
    void createCalbit();

    // Mark completion of Calbit
    /*
    static func completeCalbit(_ id:String) {
        let url = calbitBaseURL + id + "/complete"
        // HttpUtil.put(url)
    }

    static func updateCalbit(_ id:String) {
        let url = calbitBaseURL + id
        // HttpUtil.put(url)
    }

    static func deleteCalbit(_ id:String) {
        let url = calbitBaseURL + id
        // HttpUtil.delete(url)
    }
    */
}
