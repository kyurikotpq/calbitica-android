package com.calbitica.app.Profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.calbitica.app.Models.Habitica.Habitica;
import com.calbitica.app.R;
import com.calbitica.app.Util.CalbiticaAPI;
import com.calbitica.app.Util.UserData;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);//Inflate Layout
        return view;//return default view
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // All Habitica Profile and Stats here...
        TextView profileName = getActivity().findViewById(R.id.profileName);
        TextView profileLevelType = getActivity().findViewById(R.id.profileLevelType);

        ProgressBar healthBar = getActivity().findViewById(R.id.healthBar);
        TextView profileHP = getActivity().findViewById(R.id.profileHP);
        ProgressBar expBar = getActivity().findViewById(R.id.expBar);
        TextView profileExp = getActivity().findViewById(R.id.profileExp);
        ProgressBar manaBar = getActivity().findViewById(R.id.manaBar);
        TextView profileMana = getActivity().findViewById(R.id.profileMana);

        // Habitica Inn Status
        Button innStatus = getActivity().findViewById(R.id.innStatus);

        // Habitica Quest Info
        TextView questStatus = getActivity().findViewById(R.id.questStatus);
        Button questAccept = getActivity().findViewById(R.id.questAccept);
        Button questReject = getActivity().findViewById(R.id.questReject);

        // Retrieve the JWT
        String jwt = UserData.get("jwt", getContext());

        // Build the API Call
        Call<Habitica> apiCall = CalbiticaAPI.getInstance(jwt).habitica().getHabiticaProfile();
        // Make the API Call
        apiCall.enqueue(new Callback<Habitica>() {
            @Override
            public void onResponse(Call<Habitica> call, Response<Habitica> response) {
                if (!response.isSuccessful()) {
                    Log.d("Unsuccessful to get Habitica Profile: ", response.toString());
                    return;
                }

                Habitica profile = response.body();

                profileName.setText(profile.getData().getProfile().getName());
//                profileLevelType.setText(profile.getData());
            }

            @Override
            public void onFailure(Call<Habitica> call, Throwable t) {
                Log.d("Fail to get habitica profile: ", t.getMessage());
            }
        });
    }
}
