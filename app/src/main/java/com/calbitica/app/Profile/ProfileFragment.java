package com.calbitica.app.Profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.calbitica.app.R;
import com.calbitica.app.Util.CalbiticaAPI;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
//    @Nullable
//    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);//Inflate Layout
        return view;//return default view
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Build the API Call
        Call<Habitica> apiCall = CalbiticaAPI.getInstance("").habitica().getHabiticaProfile();
        // Make the API Call
        apiCall.enqueue(new Callback<Habitica>() {
            @Override
            public void onResponse(Call<Habitica> call, Response<Habitica> response) {
                if (!response.isSuccessful()) {
                    Log.d("Profile unsuccessfully: ", response.toString());
                    return;
                }


            }

            @Override
            public void onFailure(Call<Habitica> call, Throwable t) {
                Log.d("Profile FAILED to retrieve: ", call.toString());
                Log.d("Profile MORE DETAILS: ", t.getLocalizedMessage());
            }
        });
    }
}
