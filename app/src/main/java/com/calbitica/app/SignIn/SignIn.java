package com.calbitica.app.SignIn;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.calbitica.app.Internet.CheckInternetConnection;
import com.calbitica.app.Internet.ConnectivityReceiver;
import com.calbitica.app.Models.Auth.GoogleAuth;
import com.calbitica.app.NavigationBar.NavigationBar;
import com.calbitica.app.R;
import com.calbitica.app.Util.CalbiticaAPI;
import com.calbitica.app.Util.UserData;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignIn extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {
    private SignInButton btn_signinGoogle;
    private int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        btn_signinGoogle = findViewById(R.id.btn_signinGoogle);

        btn_signinGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkConnection()) {
                    // Display all Google accounts that you currently have;
                    Intent signInIntent = GoogleAuth.getInstance(getApplicationContext())
                                            .getClient()
                                            .getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from mGoogleSignInClient.getSignInIntent();
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach a listener
            // Get the selected google account information...
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            googleSignInResult(task);
        }
    }

    private void googleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            // Get the google account
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Toast.makeText(SignIn.this, "Signing you in...", Toast.LENGTH_SHORT).show();

            // Retrieve the authCode and send to Calbitica server
            // Then store the JWT in sharedPreferences
            String authCode = account.getServerAuthCode();

            System.out.println("authCode: " + authCode);

            // very bad typing, I'm sorry
            HashMap<String, String> codeObj = new HashMap<>();
            codeObj.put("code", authCode);

            // Build the API Call
            Call<HashMap<String, String>> apiCall = CalbiticaAPI.getInstance("").auth()
                                                        .tokensFromAuthCode(codeObj);

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

                            // Get other profile info as well
                            String displayName = account.getDisplayName();
                            String thumbnail = (account.getPhotoUrl() != null)
                                    ? account.getPhotoUrl().toString() : "";

                            // Handle JWT
                            HashMap<String, String> user = new HashMap<>();
                            user.put("jwt", jwt);
                            user.put("displayName", displayName);
                            user.put("thumbnail", thumbnail);

                            UserData.save(user, getApplicationContext());
                            Log.d("API JWT: ", jwt);
                            updateUI();
                        }
                    } catch (Exception e) {
                        Log.d("API JWT FAILED", e.getLocalizedMessage());
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    Log.d("API JWT FAILED", call.toString());
                    Log.d("API JWT MORE DETAILS", t.getLocalizedMessage());
                }
            });

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason
            // Refer to GoogleSignInStatusCodes to know out more info
            Toast.makeText(
                    SignIn.this,
                    "Signing in failed. Check your\nconnection and try again.",
                    Toast.LENGTH_SHORT
            ).show();
            System.out.println("GoogleSignInStatusCodes " + e.getLocalizedMessage());
        }
    }

    // Check if sign-in exists in both SharedPreferences and Google SignIn
    private void updateUI() {
        String jwt = UserData.get("jwt", getApplicationContext());
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

        if (acct != null && jwt != null) {
            // Signed in successfully, show authenticated UI at NavigationBar
            startActivity(new Intent(SignIn.this, NavigationBar.class));
            finish();
        }
    }

    // Method to check connection status
    private boolean checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showSnack(isConnected);
        return isConnected;
    }

    // Showing the status on below the screen(Something like Toast)
    private void showSnack(boolean isConnected) {
        String message;
        int color;
        if (isConnected) {
            message = "Good! Connected to Internet";
            color = Color.WHITE;
        } else {
            message = "Sorry! Not connected to internet";
            color = Color.RED;
        }

        // Another fancy message like Toast
        Snackbar snackbar = Snackbar.make(findViewById(R.id.btn_signinGoogle), message, Snackbar.LENGTH_LONG);

        // Making use of any of the TextView find in the layout, to publish as a View
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar.show();
    }

    // Confirm there is connection
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }

    // It will check you got existing account still logged in
    // Yes -> Redirect to Navigation Bar, No -> Redirect from the start of this Sign in Activity Page
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // register connection status listener
        CheckInternetConnection.getInstance().setConnectivityListener(this);
    }
}
