package com.calbitica.app.Settings;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

// Models Settings API requests for the Retrofit plugin
public interface SettingsInterface {
    // Exchange serverAuthCode for a Calbitia JWT
    @POST("habitica")
    Call<HashMap<String, String>> saveSettings (
            @Body HashMap<String, String> settings
    );
}
