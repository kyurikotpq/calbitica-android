package com.calbitica.app.Models.Calendars;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CalendarsInterface {
    // Get all the Google Calendars
    @GET("cal")
    Call<Calendars> getAllCalendars();

    @GET("cal/sync/{id}")
    Call<SyncCalendar> syncCalendar(@Path("id") String id,
                                 @Query("sync") Boolean sync) ;

}
