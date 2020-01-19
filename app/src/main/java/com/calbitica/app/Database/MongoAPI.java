package com.calbitica.app.Database;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

public interface MongoAPI {
//    Sample
//    @GET    -> Http Methods
//    ("posts") -> link

    @GET("posts")
    Call<List<MongoDB>> getPosts(
            @Query("userId") Integer[] userId,
            @Query("_sort") String sort,
            @Query("_order") String order
    );

    @GET("posts")
    Call<List<MongoDB>> getPosts(@QueryMap Map<String, String> parameters);

    @GET
    Call<List<MongoDB>> getComments(@Url String url);

    @POST("posts")
    Call<MongoDB> createPost(@Body MongoDB post);

    @FormUrlEncoded
    @POST("posts")
    Call<MongoDB> createPost(
            @Field("userId") int userId,
            @Field("title") String title,
            @Field("body") String text
    );

    @FormUrlEncoded
    @POST("posts")
    Call<MongoDB> createPost (@FieldMap Map<String, String> fields);

    @Headers({"Static-Header1: 123", "Static-Header2: 456"})
    @PUT("posts/{id}")
    Call<MongoDB> putPost(@Header("Dynamic-Header") String header,
                          @Path("id") int id,
                          @Body MongoDB post);

    @PATCH("posts/{id}")
    Call<MongoDB> patchPost(@HeaderMap Map<String, String> headers,
                            @Path("id") int id,
                            @Body MongoDB post);

    @DELETE("posts/{id}")
    Call<Void> deletePost(@Path("id") int id);
}
