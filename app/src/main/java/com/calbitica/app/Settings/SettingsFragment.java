package com.calbitica.app.Settings;

import android.os.Bundle;
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

public class SettingsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        EditText userID = getActivity().findViewById(R.id.HABITICA_USER_ID);
        EditText apiKey = getActivity().findViewById(R.id.HABITICA_API_KEY);
        Button btnSave = getActivity().findViewById(R.id.btn_HABITICA_SAVE);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),"USER ID " + userID.getText().toString() + "\n" + "API KEY " + apiKey.getText().toString(), Toast.LENGTH_LONG).show();
            }
        });
    }
}