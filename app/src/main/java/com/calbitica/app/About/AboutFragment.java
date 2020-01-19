package com.calbitica.app.About;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.calbitica.app.R;

public class AboutFragment extends Fragment {
    private static final int REQUEST_CALL = 1;
    private TextView callValue, messageValue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        callValue = getActivity().findViewById(R.id.callValue);
        callValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePhoneCall();
            }
        });

        // It will call the actual Gmail to send, automatically filled up the company email as well
        messageValue = getActivity().findViewById(R.id.messageValue);
        messageValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailSelectorIntent = new Intent(Intent.ACTION_SENDTO);
                emailSelectorIntent.setData(Uri.parse("mailto:"));

                final Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"calbitica@support.com"});
                emailIntent.setSelector( emailSelectorIntent );

                startActivity(emailIntent);
            }
        });
    }

    // If you want to make the call to dynamically, set the hard-coded into EditText
    private void makePhoneCall() {
        String number = "61157894";

        // Include the spacing key => 0 length
        if(number.trim().length() > 0) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[] {
                        Manifest.permission.CALL_PHONE
                }, REQUEST_CALL);
            } else {
                // All the alphabet will become number as well
                String dial = "tel:" + number;
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
            }
        } else {
            Toast.makeText(getContext(), "Enter Phone Number", Toast.LENGTH_SHORT).show();
        }
    }

    // When the Call Permission Dialog appear...
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            } else {
                Toast.makeText(getContext(), "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }

    }
}