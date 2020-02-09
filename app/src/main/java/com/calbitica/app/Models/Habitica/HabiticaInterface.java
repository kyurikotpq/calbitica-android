package com.calbitica.app.Models.Habitica;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface HabiticaInterface {
    // Get the profile from the habiticaAPI
    @GET("profile")
    Call<HabiticaProfileResponse> getHabiticaProfile();

    // Toggle sleep status
    @GET("sleep")
    Call<HabiticaToggleSleepResponse> toggleSleep();

    // Accept/Decline a Quest invitation
    @FormUrlEncoded
    @POST("quest")
    Call<HabiticaQuestResponse> inviteQuest(
            @Field("accept") Boolean accept,
            @Field("groupID") String groupID
    );
}
