package com.calbitica.app.SyncCalendars;

import retrofit2.Call;
import retrofit2.http.GET;

public interface CalendarsInterface {
    // Get all the Google Calendars
    @GET("cal")
    Call<Calendars> getAllCalendars();
}
