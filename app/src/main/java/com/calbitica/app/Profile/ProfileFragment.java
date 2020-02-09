package com.calbitica.app.Profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.calbitica.app.Models.Habitica.HabiticaInfo;
import com.calbitica.app.Models.Habitica.HabiticaProfileResponse;
import com.calbitica.app.Models.Habitica.HabiticaToggleSleepResponse;
import com.calbitica.app.Models.Habitica.Stats;
import com.calbitica.app.R;
import com.calbitica.app.Util.CalbiticaAPI;
import com.calbitica.app.Util.UserData;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static java.lang.Math.round;

public class ProfileFragment extends Fragment {
    // All HabiticaProfileResponse Profile and Stats here...
    TextView profileName;
    TextView profileLevelType;

    ProgressBar healthBar; TextView profileHP;
    ProgressBar expBar; TextView profileExp;
    ProgressBar manaBar; TextView profileMana;

    // HabiticaProfileResponse Inn Status
    Button innStatusBTN;

    // HabiticaProfileResponse Quest Info
    TextView questStatus; Button questAccept; Button questReject;

    // Temporary data values
    boolean isSleeping = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);//Inflate Layout
        return view;//return default view
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // All HabiticaProfileResponse Profile and Stats here...
        profileName = getActivity().findViewById(R.id.profileName);
        profileLevelType = getActivity().findViewById(R.id.profileLevelType);

        healthBar = getActivity().findViewById(R.id.healthBar);
        profileHP = getActivity().findViewById(R.id.profileHP);
        expBar = getActivity().findViewById(R.id.expBar);
        profileExp = getActivity().findViewById(R.id.profileExp);
        manaBar = getActivity().findViewById(R.id.manaBar);
        profileMana = getActivity().findViewById(R.id.profileMana);

        // HabiticaProfileResponse Inn Status
        innStatusBTN = getActivity().findViewById(R.id.innStatus);

        // HabiticaProfileResponse Quest Info
        questStatus = getActivity().findViewById(R.id.questStatus);
        questAccept = getActivity().findViewById(R.id.questAccept);
        questReject = getActivity().findViewById(R.id.questReject);

        // Retrieve the JWT
        String jwt = UserData.get("jwt", getContext());

        // Build the API Call
        Call<HabiticaProfileResponse> apiCall = CalbiticaAPI.getInstance(jwt).habitica().getHabiticaProfile();
        // Make the API Call
        apiCall.enqueue(new Callback<HabiticaProfileResponse>() {
            @Override
            public void onResponse(Call<HabiticaProfileResponse> call, Response<HabiticaProfileResponse> response) {
                if (!response.isSuccessful()) {
                    try {

                        Log.d("Unsuccessful to get HabiticaProfileResponse Profile: ", response.errorBody().string());
                        return;
                    } catch (Exception e) {

                    }
                }
                Log.d("profile response!", response.toString());
                HabiticaProfileResponse profileResponse = response.body();
                try {
                    if(profileResponse != null) {
                        HabiticaInfo profileInfo = profileResponse.getData();

                        // set data on the views
                        profileName.setText(profileInfo.getProfile().getName());

                        Stats profileStats = profileInfo.getStats();
                        String levelStr = "Level " + String.format("%.0f", profileStats.getLvl())
                                    + " • " + profileStats.getClassname();
                        profileLevelType.setText(levelStr);

                        float hp = profileStats.getHp() / profileStats.getMaxHealth();
                        healthBar.setProgress(round(hp * 100), true);
                        profileHP.setText(String.format("%.0f", profileStats.getHp()) + " / "
                                        + String.format("%.0f", profileStats.getMaxHealth()));

                        float exp = profileStats.getExp() / profileStats.getToNextLevel();
                        expBar.setProgress(round(exp * 100), true);
                        profileExp.setText(String.format("%.0f", profileStats.getExp()) + " / "
                                        + String.format("%.0f", profileStats.getToNextLevel()));

                        float mp = profileStats.getMp() / profileStats.getMaxMP();
                        manaBar.setProgress(round(mp * 100), true);
                        profileMana.setText(String.format("%.0f", profileStats.getMp()) + " / "
                                        + String.format("%.0f", profileStats.getMaxMP()));

                        // Set Sleep button state
                        isSleeping = profileInfo.getPreferences().getSleep();
                        updateFragments("sleep");

                        // Quest stuff
                    }
                } catch(Exception e) {
                    // usually is nullpointer

                }
            }

            @Override
            public void onFailure(Call<HabiticaProfileResponse> call, Throwable t) {
                Log.d("Fail to get habitica profile: ", t.getMessage());
            }
        });

        // Button click listeners
        innStatusBTN.setOnClickListener(v -> onSleepBtnClicked());
    }

    public void updateFragments(String type) {
        getActivity().runOnUiThread(() -> {
            List<Fragment> allFragments = getActivity().getSupportFragmentManager().getFragments();
            if (allFragments == null || allFragments.isEmpty())
                return;
            for (Fragment fragment : allFragments) {
                if (fragment.isVisible()) {
                    switch(type) {
                        case "sleep":
                            ((ProfileFragment) fragment).updateSleepBtn();
                            break;
                    }
                }

            }
        });
    }
    public void updateSleepBtn() {
        String innStatusTxt = isSleeping ? "Resume Damage" : "Pause Damage";
        int indentifier = isSleeping ? R.color.red_3 : R.color.blue_3;
        innStatusBTN.setText(innStatusTxt);
        innStatusBTN.setBackgroundColor(getActivity().getResources().getColor(indentifier, null));
    }

    public void onSleepBtnClicked() {
        // Retrieve the JWT
        String oldJWT = UserData.get("jwt", getContext());
        // Build the API Call
        Call<HabiticaToggleSleepResponse> apiCall = CalbiticaAPI.getInstance(oldJWT)
                            .habitica().toggleSleep();

        // Make the API Call
        apiCall.enqueue(new Callback<HabiticaToggleSleepResponse>() {
            @Override
            public void onResponse(Call<HabiticaToggleSleepResponse> call,
                                   Response<HabiticaToggleSleepResponse> response) {
                if (!response.isSuccessful()) {
                    Log.d("SLEEP CALL", response.toString());
                    return;
                }
                try {
                    HabiticaToggleSleepResponse responseData = response.body();
                    // Handle new JWT returned, if any
                    if (responseData.getJwt() != null
                    && !responseData.getJwt().equals("")) {
                        // Handle JWT
                        HashMap<String, String> user = new HashMap<>();
                        user.put("jwt", responseData.getJwt());

                        UserData.save(user, getContext());
                        Log.d("API JWT: ", responseData.getJwt());
                    }

                    // Update the button! + toast
                    if(responseData.getData().containsKey("sleep")) {
                        System.out.println("WE GOT SLEEP");
                        HashMap<String, Object> dataHM = responseData.getData();
                        String msg = dataHM.get("message").toString();
                        isSleeping = (boolean) dataHM.get("sleep");
                        updateFragments("sleep");
                    }
                } catch (Exception e) {
                    Log.d("API JWT FAILED", e.getLocalizedMessage());
                }
            }

            @Override
            public void onFailure(Call<HabiticaToggleSleepResponse> call, Throwable t) {
                Log.d("Save settings FAILED", call.toString());
                Log.d("Save settings MORE DETAILS", t.getLocalizedMessage());
            }
        });
    }
}
