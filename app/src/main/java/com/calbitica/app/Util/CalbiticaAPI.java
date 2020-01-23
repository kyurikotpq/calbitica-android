package com.calbitica.app.Util;

import com.calbitica.app.Models.AuthInterface;
import com.calbitica.app.Models.CalbitInterface;

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
    private static final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    Request originalRequest = chain.request();

                    Request newRequest = originalRequest.newBuilder()
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json")
                            // add authorization soon....
                            .build();

                    return chain.proceed(newRequest);
                }
            })
            .build();

    private static CalbiticaAPI instance = null;
    private AuthInterface authInterface = null;
    private CalbitInterface calbitInterface = null;

    private CalbiticaAPI() {
        // build the different interfaces for use later
        setAuthInterface();
        setCalbitInterface();
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
                .baseUrl(BASE_URL + "calbit/")
                .addConverterFactory(GsonConverterFactory.create()) // convert JSON data (got from server) into java (model) objects (POJO)
                .client(okHttpClient)
                .build();

        // Populate all the respective HTTP Methods & links
        calbitInterface = retrofit.create(CalbitInterface.class);
    }

    public static CalbiticaAPI getInstance() {
        if (instance == null)
            instance = new CalbiticaAPI();

        return instance;
    }

    public AuthInterface auth() {
        return this.authInterface;
    }

}
