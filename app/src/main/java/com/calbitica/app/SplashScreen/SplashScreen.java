package com.calbitica.app.SplashScreen;

import androidx.appcompat.app.AppCompatActivity;

import com.calbitica.app.Auth.SignInActivity;
import com.calbitica.app.NavigationBar.NavigationBar;
import com.calbitica.app.R;
import com.calbitica.app.Util.UserData;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreen extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 500;   // 500 milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String jwt = UserData.get("jwt", getApplicationContext());
                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

                Intent nextIntent = (acct != null && jwt != null)
                        ? new Intent(SplashScreen.this, NavigationBar.class)
                        : new Intent(SplashScreen.this, SignInActivity.class);

                startActivity(nextIntent);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
