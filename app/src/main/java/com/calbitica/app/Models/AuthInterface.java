package com.calbitica.app.Models;


import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

// Models API requests for the Retrofit plugin
public interface AuthInterface {
    // Exchange serverAuthCode for a Calbitia JWT
    @POST("code")
    Call<HashMap<String, Object>> tokensFromAuthCode (
            @Body HashMap<String, Object> codeHM
    );
}
