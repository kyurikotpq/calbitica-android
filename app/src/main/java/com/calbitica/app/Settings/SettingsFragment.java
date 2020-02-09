package com.calbitica.app.Settings;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.calbitica.app.R;
import com.calbitica.app.Util.CalbiticaAPI;
import com.calbitica.app.Util.UserData;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        EditText userIDET = getActivity().findViewById(R.id.HABITICA_USER_ID);
        EditText apiKeyET = getActivity().findViewById(R.id.HABITICA_API_KEY);
        Button saveBTN = getActivity().findViewById(R.id.btn_HABITICA_SAVE);

        saveBTN.setOnClickListener(v -> {
            String hUserID = userIDET.getText().toString();
            String apiKey = apiKeyET.getText().toString();

            HashMap<String, String> data = new HashMap<>();
            if(hUserID != null && !hUserID.equals("")) data.put("hUserID", hUserID);
            if(apiKey != null && !apiKey.equals("")) data.put("apiKey", apiKey);

            // Retrieve the JWT
            String oldJWT = UserData.get("jwt", getContext());
            // Build the API Call
            Call<HashMap<String, String>> apiCall = CalbiticaAPI.getInstance(oldJWT)
                                                    .settings().saveSettings(data);

            // Make the API Call
            apiCall.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call,
                                       Response<HashMap<String, String>> response) {
                    if (!response.isSuccessful()) {
                        Log.d("API JWT CALL", response.toString());
                        return;
                    }
                    try {
                        HashMap<String, String> data = response.body();
                        if (data.containsKey("jwt")) {
                            String jwt = data.get("jwt");

                            // Handle JWT
                            HashMap<String, String> user = new HashMap<>();
                            user.put("jwt", jwt);

                            UserData.save(user, getContext());
                            Log.d("API JWT: ", jwt);

                            String message = (data.containsKey("message"))
                                    ? data.get("message")
                                    : "Something went wrong. Please try again.";
                            Toast.makeText(getActivity(),message, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Log.d("API JWT FAILED", e.getLocalizedMessage());
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    Log.d("Save settings FAILED", call.toString());
                    Log.d("Save settings MORE DETAILS", t.getLocalizedMessage());
                }
            });

        });
    }
}