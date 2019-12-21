package edu.nyp.calbiticaandroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class WeekFragment extends Fragment {
    public static WeekFragment newInstance(String selectedDate) {
        WeekFragment fragment = new WeekFragment();
        Bundle data = new Bundle();
        data.putString("selectedDate", selectedDate);
        fragment.setArguments(data);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_week, container, false);//Inflate Layout

        if(getArguments() != null) {
            String selectedDate = getArguments().getString("selectedDate");
            // Able to retrieve the data from the Navigation Bar
//            Toast.makeText(getContext(), "Hey from fragment " + selectedDate, Toast.LENGTH_LONG).show();
            return view;//return data view
        }

        return view;//return default view
    }
}