package com.calbitica.app.Util;

import android.util.Log;

import com.calbitica.app.Models.Auth.AuthInterface;
import com.calbitica.app.Models.Calbit.CalbitInterface;
import com.calbitica.app.Models.Habitica.HabiticaInterface;
import com.calbitica.app.Models.Settings.SettingsInterface;
import com.calbitica.app.Models.Calendars.CalendarsInterface;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CalbiticaAPI {
    // Constants - our URL
    private static final String BASE_URL = "https://app.kyurikotpq.com/calbitica/api/";

    // HTTP interceptor - add header to all requests
    private OkHttpClient okHttpClient;
    private String currentJWT = "";
    private static CalbiticaAPI instance = new CalbiticaAPI();

    // Interfaces
    private AuthInterface authInterface = null;
    private CalbitInterface calbitInterface = null;
    private SettingsInterface settingsInterface = null;
    private CalendarsInterface calendarsInterface = null;
    private HabiticaInterface habiticaInterface = null;

    private CalbiticaAPI() { rebuildInterceptor(""); }
    private void rebuildInterceptor(String jwt) {
        this.currentJWT = jwt;
        okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request originalRequest = chain.request();

                        Request newRequest = originalRequest.newBuilder()
                                .header("Content-Type", "application/json")
                                .header("Accept", "application/json")
                                // add authorization soon....
                                .header("Authorization", "Bearer " + jwt)
                                .build();

                        Log.d("BUILDING THE HTTP CLIENT", jwt);
                        return chain.proceed(newRequest);
                    }
                })
                .build();

        // build the different interfaces for use later
        setAuthInterface();
        setCalbitInterface();
        setSettingsInterface();
        setCalendarsInterface();
        setHabiticaInterface();
    }

    private void setAuthInterface() {
        // Build the Retrofit wrapper
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL + "auth/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        // Populate all the respective HTTP Methods & links
        authInterface = retrofit.create(AuthInterface.class);
    }

    private void setCalbitInterface() {
        //  Build the Retrofit wrapper
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)                                  // Interface cannot be empty string, so declare as nothing here
                .addConverterFactory(GsonConverterFactory.create()) // convert JSON data (got from server) into java (model) objects (POJO)
                .client(okHttpClient)
                .build();

        // Populate all the respective HTTP Methods & links
        calbitInterface = retrofit.create(CalbitInterface.class);
    }

    private void setSettingsInterface() {
        //  Build the Retrofit wrapper
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL + "settings/")
                .addConverterFactory(GsonConverterFactory.create()) // convert JSON data (got from server) into java (model) objects (POJO)
                .client(okHttpClient)
                .build();

        // Populate all the respective HTTP Methods & links
        settingsInterface = retrofit.create(SettingsInterface.class);
    }

    private void setCalendarsInterface() {
        // Build the Retrofit wrapper
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)                                  // Interface cannot be empty string, so declare as nothing here
                .addConverterFactory(GsonConverterFactory.create()) // convert JSON data (got from server) into java (model) objects (POJO)
                .client(okHttpClient)
                .build();

        // Populate all the respective HTTP Methods & links
        calendarsInterface = retrofit.create(CalendarsInterface.class);
    }

    private void setHabiticaInterface() {
        // Build the Retrofit wrapper
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL + "h/")
                .addConverterFactory(GsonConverterFactory.create()) // convert JSON data (got from server) into java (model) objects (POJO)
                .client(okHttpClient)
                .build();

        // Populate all the respective HTTP Methods & links
        habiticaInterface = retrofit.create(HabiticaInterface.class);
    }

    private boolean jwtHasChanged(String newJWT) {
        return !newJWT.equals(this.currentJWT);
    }

    public static CalbiticaAPI getInstance(String jwt) {
        if (instance.jwtHasChanged(jwt)) {
            instance.rebuildInterceptor(jwt);
        }

        return instance;
    }

    public AuthInterface auth() {
        return this.authInterface;
    }

    public CalbitInterface calbit() {
        return this.calbitInterface;
    }

    public SettingsInterface settings() {
        return this.settingsInterface;
    }

    public CalendarsInterface calendars() { return this.calendarsInterface; }

    public HabiticaInterface habitica() { return this.habiticaInterface; }
}
