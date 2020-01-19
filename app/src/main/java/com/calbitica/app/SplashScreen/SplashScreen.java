package com.calbitica.app.SplashScreen;

import androidx.appcompat.app.AppCompatActivity;
import com.calbitica.app.Google_Acccount.SignInActivity;
import com.calbitica.app.R;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreen extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 500;   //500 milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent googleSignInIntent = new Intent(SplashScreen.this, SignInActivity.class);
                startActivity(googleSignInIntent);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
