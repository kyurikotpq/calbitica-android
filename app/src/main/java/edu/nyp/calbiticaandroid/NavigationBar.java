package edu.nyp.calbiticaandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static java.security.AccessController.getContext;

public class NavigationBar extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    ImageView arrow = null;
    boolean arrowTrigger = false;
    CalendarView calendarView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_bar);

        // Setting the Calendar title as the current month, and function of arrow from the layout
        final Calendar calendar = Calendar.getInstance();
        String currentMonth = DateFormat.getDateInstance(DateFormat.LONG).format(calendar.getTime());
        final TextView title = findViewById(R.id.title);
        title.setText(currentMonth.replaceAll("[^a-zA-Z]", ""));

        SimpleDateFormat month_date = new SimpleDateFormat("d/MM/YYYY");
        currentMonth = month_date.format(calendar.getTime());
        Toast.makeText(NavigationBar.this, "Today: " + currentMonth, Toast.LENGTH_LONG).show();

        arrow = (ImageView) findViewById(R.id.arrow);
        calendarView = (CalendarView) findViewById(R.id.calendarView);
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
                calendar.set(Calendar.MONTH, month);
                SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
                String selectedMonth = month_date.format(calendar.getTime());
                title.setText(selectedMonth);

                String selected = dayOfMonth + "/" + (month+1) + "/" + year;
                Toast.makeText(NavigationBar.this, "Selected: " + selected, Toast.LENGTH_LONG).show();

                // As then also pass the data into WeekFragment
                WeekFragment fragment = WeekFragment.newInstance(selected);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
            }
        });

        // Navigation Bar Stuff here...
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.nav_leftview);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_open, R.string.navigation_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // When open the app, applied all this NavigationBar function into WeekFragment, and return the WeekFragment classes and layout
        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new WeekFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_week);
        }
    }

    // Left Navigation Bar Selection
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_settings:
                // addToBackStack(null) -> Allow to go previous page, rather than exit the app
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).addToBackStack(null).commit();
                break;
            case R.id.nav_help:
                // addToBackStack(null) -> Allow to go previous page, rather than exit the app
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HelpFragment()).addToBackStack(null).commit();
                break;
            case R.id.nav_logout:
                // addToBackStack(null) -> Allow to go previous page, rather than exit the app
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new LogoutFragment()).addToBackStack(null).commit();
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
                break;
            case R.id.nav_schedule:
                // addToBackStack(null) -> Allow to go previous page, rather than exit the app
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ScheduleFragment()).addToBackStack(null).commit();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // When pressing back button of mobile, while the nav bar is open
    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}