package com.calbitica.app.Auth;


import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

// Models API requests for the Retrofit plugin
public interface AuthInterface {
    // Exchange serverAuthCode for a Calbitia JWT
    @POST("code")
    Call<HashMap<String, String>> tokensFromAuthCode (
            @Body HashMap<String, String> codeHM
    );
}
