package com.calbitica.app.API_Docs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.calbitica.app.Database.MongoAPI;
import com.calbitica.app.Database.MongoDB;
import com.calbitica.app.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class API_DocsFragment extends Fragment {
    private TextView textViewResult;
    private MongoAPI mongoApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_api_docs, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        textViewResult = getActivity().findViewById(R.id.text_viewResult);

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();

        // To return Logs request and response lines and their respective headers and bodies (if present).
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Example: The URL http://www.publicobject.com/helloworld.txt
            // redirects to https://publicobject.com/helloworld.txt
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request originalRequest = chain.request();

                        Request newRequest = originalRequest.newBuilder()
                                .header("Interceptor-Header", "xyz")
                                .build();

                        return chain.proceed(newRequest);
                    }
                })
                .addInterceptor(loggingInterceptor)
                .build();

        // URL Request links
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://jsonplaceholder.typicode.com/")
                .addConverterFactory(GsonConverterFactory.create())     // convert JSON data (got from server) into java (model) objects (POJO)
                .client(okHttpClient)
                .build();

        mongoApi = retrofit.create(MongoAPI.class);     // Populate all the respective HTTP Methods & links

        // Try out by comment and uncomment that suit our needs
        getPosts();
//        getComments();
//        createPost();
//        updatePost();
//        deletePost();
    }

    private void getPosts() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("userId", "1");
        parameters.put("_sort", "id");
        parameters.put("_order", "desc");

//        Call<List<MongoDB>> callApi = mongoApi.getPosts(new Integer[]{2,3,6}, null, null);
        Call<List<MongoDB>> callApi = mongoApi.getPosts(parameters);

        callApi.enqueue(new Callback<List<MongoDB>>() {
            @Override
            public void onResponse(Call<List<MongoDB>> call, Response<List<MongoDB>> response) {
                if (!response.isSuccessful()) {
                    textViewResult.setText("Code: " + response.code());
                    return;
                }

                List<MongoDB> posts = response.body();

                for (MongoDB post : posts) {
                    String content = "";
                    content += "ID: " + post.getId() + "\n";
                    content += "User ID: " + post.getUserId() + "\n";
                    content += "Title: " + post.getTitle() + "\n";
                    content += "Text: " + post.getText() + "\n\n";

                    textViewResult.append(content);
                }
            }

            @Override
            public void onFailure(Call<List<MongoDB>> call, Throwable t) {
                textViewResult.setText(t.getMessage());
            }
        });
    }

    private void getComments() {
        Call<List<MongoDB>> callApi = mongoApi.getComments("https://jsonplaceholder.typicode.com/posts/3/comments");

        callApi.enqueue(new Callback<List<MongoDB>>() {
            @Override
            public void onResponse(Call<List<MongoDB>> call, Response<List<MongoDB>> response) {
                if(!response.isSuccessful()) {
                    textViewResult.setText("Code: " + response.code());
                    return;
                }

                List<MongoDB> comments = response.body();

                for(MongoDB comment : comments) {
                    String content = "";
                    content += "ID: " + comment.getId() + "\n";
                    content += "Post ID: " + comment.getPostId() + "\n";
                    content += "Name: " + comment.getName() + "\n";
                    content += "Email: " + comment.getEmail() + "\n";
                    content += "Text: " + comment.getText() + "\n\n";

                    textViewResult.append(content);
                }
            }

            @Override
            public void onFailure(Call<List<MongoDB>> call, Throwable t) {
                textViewResult.setText(t.getMessage());
            }
        });
    }

    private void createPost() {
        MongoDB post = new MongoDB(23, "New Title", "New Text");

        Map<String, String> fields = new HashMap<>();
        fields.put("userId", "25");
        fields.put("title", "New Title");

//        Call<MongoDB> call = mongoApi.createPost(post);
        Call<MongoDB> call = mongoApi.createPost(fields);

        call.enqueue(new Callback<MongoDB>() {
            @Override
            public void onResponse(Call<MongoDB> call, Response<MongoDB> response) {
                if(!response.isSuccessful()) {
                    textViewResult.setText("Code: " + response.code());
                    return;
                }

                MongoDB postResponse = response.body();

                String content = "";
                content += "Code: " + response.code() + "\n";
                content += "ID: " + postResponse.getId() + "\n";
                content += "User ID: " + postResponse.getUserId() + "\n";
                content += "Title: " + postResponse.getTitle() + "\n";
                content += "Text: " + postResponse.getText() + "\n\n";

                textViewResult.setText(content);
            }

            @Override
            public void onFailure(Call<MongoDB> call, Throwable t) {
                textViewResult.setText(t.getMessage());
            }
        });
    }

    private void updatePost() {
        MongoDB post = new MongoDB(12, null, "New Text");

        Map<String, String> headers = new HashMap<>();
        headers.put("Map-Header1", "def");
        headers.put("Map-Header2", "ghi");

//        Call<MongoDB> call = mongoApi.putPost("abc", 5, post);
        Call<MongoDB> call = mongoApi.patchPost(headers ,5, post);

        call.enqueue(new Callback<MongoDB>() {
            @Override
            public void onResponse(Call<MongoDB> call, Response<MongoDB> response) {
                if(!response.isSuccessful()) {
                    textViewResult.setText("Code: " + response.code());
                    return;
                }

                MongoDB postResponse = response.body();

                String content = "";
                content += "Code: " + response.code() + "\n";
                content += "ID: " + postResponse.getId() + "\n";
                content += "User ID: " + postResponse.getUserId() + "\n";
                content += "Title: " + postResponse.getTitle() + "\n";
                content += "Text: " + postResponse.getText() + "\n\n";

                textViewResult.setText(content);
            }

            @Override
            public void onFailure(Call<MongoDB> call, Throwable t) {
                textViewResult.setText(t.getMessage());
            }
        });
    }

    private void deletePost() {
        Call<Void> call = mongoApi.deletePost(5);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                textViewResult.setText("Code: " + response.code());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                textViewResult.setText(t.getMessage());
            }
        });
    }
}