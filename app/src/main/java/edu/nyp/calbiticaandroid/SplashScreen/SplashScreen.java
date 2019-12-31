package edu.nyp.calbiticaandroid.SplashScreen;

import androidx.appcompat.app.AppCompatActivity;
import edu.nyp.calbiticaandroid.Navigation_Bar.NavigationBar;
import edu.nyp.calbiticaandroid.R;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreen extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 500;   //500 milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent homeIntent = new Intent(SplashScreen.this, NavigationBar.class);
                startActivity(homeIntent);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
