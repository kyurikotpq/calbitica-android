package com.calbitica.app.Navigation_Bar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.calbitica.app.About.AboutFragment;
import com.calbitica.app.Google_Acccount.SignInActivity;
import com.calbitica.app.R;
import com.calbitica.app.Schedule_Calendar.ScheduleFragment;
import com.calbitica.app.Settings.SettingsFragment;
import com.calbitica.app.Week_Calendar.WeekFragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class NavigationBar extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    boolean arrowTrigger = false;
    String selectedPages = null;

    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_bar);

        // Setting the Calendar title as the current month, and function of arrow from the layout
        final Calendar calendar = Calendar.getInstance();
        String currentMonth = DateFormat.getDateInstance(DateFormat.LONG).format(calendar.getTime());
        final TextView title = findViewById(R.id.title);
        title.setText(currentMonth.replaceAll("[^a-zA-Z]", ""));

        SimpleDateFormat month_date = new SimpleDateFormat("d/MM/YYYY");
        currentMonth = month_date.format(calendar.getTime());
        Toast.makeText(NavigationBar.this, "Today: " + currentMonth, Toast.LENGTH_LONG).show();

        final ImageView arrow = (ImageView) findViewById(R.id.arrow);
        final CalendarView calendarView = (CalendarView) findViewById(R.id.calendarView);
        calendarView.setVisibility(View.GONE);

        arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(arrowTrigger == false) {
                    arrow.setImageResource(R.drawable.up_arrow);
                    calendarView.setVisibility(v.VISIBLE);
                    arrowTrigger = true;
                } else {
                    arrow.setImageResource(R.drawable.down_arrow);
                    calendarView.setVisibility(v.GONE);
                    arrowTrigger = false;
                }
            }
        });

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                calendar.set(year, month, dayOfMonth);
                SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
                String selectedMonth = month_date.format(calendar.getTime());
                title.setText(selectedMonth);

                String selected = dayOfMonth + "/" + (month+1) + "/" + year;
                Toast.makeText(NavigationBar.this, "Selected: " + selected, Toast.LENGTH_LONG).show();

                if (selectedPages == "nav_week") {
                    // As then also pass the data into WeekFragment
                    WeekFragment fragment = WeekFragment.newInstance(calendar.getTime().toString());
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
                }
            }
        });

        // Navigation Bar Stuff here...
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.nav_leftview);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_open, R.string.navigation_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Call the nav_left_header layout from activity_navigation_bar layout(NavigationView -> headerLayout)
        View headerView = navigationView.getHeaderView(0);

        // Then... Get the data from the Google Account and set it into nav_left_header layout
        ImageView googlePhoto = headerView.findViewById(R.id.googlePhoto);
        TextView googleName = headerView.findViewById(R.id.googleName);

        // Configure sign-in to request the user's ID and basic profile, which are included in DEFAULT_SIGN_IN
        // In Short it will get the res(generated) -> values.xml of the default_web_client_id
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder((GoogleSignInOptions.DEFAULT_SIGN_IN))
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso
        mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (acct != null) {
            Uri acctPhoto = acct.getPhotoUrl();
            String acctName = acct.getDisplayName();

            Glide.with(getApplicationContext()).load(acctPhoto).into(googlePhoto);
            googleName.setText(acctName);
        }

        // When open the app, applied all this NavigationBar function into WeekFragment, and return the WeekFragment classes and layout
        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new WeekFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_week);
            selectedPages = "nav_week";
        }
    }

    // Left Navigation Bar Selection
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_settings:
                // addToBackStack(null) -> Allow to go previous page, rather than exit the app
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).addToBackStack(null).commit();
                selectedPages = "nav_settings";
                break;
            case R.id.nav_about:
                // addToBackStack(null) -> Allow to go previous page, rather than exit the app
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AboutFragment()).addToBackStack(null).commit();
                selectedPages = "nav_about";
                break;
            case R.id.nav_logout:
                // It will logout from the Google Account, and direct back to Sign In for Google Page
                SignInActivity.mGoogleSignInClient.signOut();
                startActivity(new Intent(NavigationBar.this, SignInActivity.class));
                Toast.makeText(getApplicationContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // Right Navigation Bar Creation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.right_navigation_menu, menu);
        return true;
    }

    // Right Navigation Bar Selection
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_week:
                // addToBackStack(null) -> Allow to go previous page, rather than exit the app
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new WeekFragment()).addToBackStack(null).commit();
                selectedPages = "nav_week";
                break;
            case R.id.nav_schedule:
                // addToBackStack(null) -> Allow to go previous page, rather than exit the app
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ScheduleFragment()).addToBackStack(null).commit();
                selectedPages = "nav_schedule";
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // When pressing back button of mobile will not exit the app, while the nav bar is open
    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}