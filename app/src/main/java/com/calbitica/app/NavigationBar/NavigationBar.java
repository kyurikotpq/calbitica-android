package com.calbitica.app.NavigationBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.arasthel.asyncjob.AsyncJob;
import com.bumptech.glide.Glide;
import com.calbitica.app.Models.Calbit.Calbit;
import com.calbitica.app.Models.Calendars.Reminder;
import com.calbitica.app.Profile.ProfileFragment;
import com.calbitica.app.SyncCalendars.SyncCalendarsFragment;
import com.calbitica.app.About.AboutFragment;
import com.calbitica.app.Models.Auth.GoogleAuth;
import com.calbitica.app.SignIn.SignIn;
import com.calbitica.app.Notification.Notification;
import com.calbitica.app.R;
import com.calbitica.app.Agenda.AgendaFragment;
import com.calbitica.app.Settings.SettingsFragment;
import com.calbitica.app.Util.CAWrapper;
import com.calbitica.app.Util.CalbiticaAPI;
import com.calbitica.app.Util.DateUtil;
import com.calbitica.app.Util.UserData;
import com.calbitica.app.Util.CalbitResultInterface;
import com.calbitica.app.Week.WeekFragment;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.calbitica.app.Week.WeekSaveEvent;
import com.google.android.material.navigation.NavigationView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

public class NavigationBar extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        CalbitResultInterface {
    public static DrawerLayout drawerLayout;                        // Relate to all the NavigationBar stuff
    private boolean arrowTrigger = false;                           // When pressed the Middle NavigationBar arrow
    public static TextView title;                                   // Middle NavigationBar
    public static ImageView arrow;                                  // Middle NavigationBar
    public static String selectedPages;                             // Tell which fragment you are in

    private ArrayList<String> selectedList = new ArrayList<>();     // Mainly for the sync, when click on back button stuff
    public static Calendar calendar;                                // Mainly for Week, Agenda Calendar, etc...
    private NavigationView navigationView;                          // To indicate the selection of navigation pages

    public static MenuItem nav_today, nav_refresh, nav_add;         // To use for respective pages(show/not show)
    private CalendarView calendarView;                              // To hide or show for display of nav small calendar
    public static int notifCount = 0;                               // This will populate respective notification and render the event
    public static List<PendingIntent> notifIntents;                 // To remove the duplicate notification
    private AlarmManager alarmManager;                              // To remove the duplicate notification

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_bar);

        createNotificationChannels();
        notifIntents = new ArrayList<>();
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav_leftview);
        navigationView.setNavigationItemSelectedListener(this);

        // Navigation Bar Stuff here...
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_open, R.string.navigation_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Call the nav_left_header layout from activity_navigation_bar layout(NavigationView -> headerLayout)
        View headerView = navigationView.getHeaderView(0);

        // Then... Get the data from the Google Account and set it into nav_left_header layout
        ImageView googlePhoto = headerView.findViewById(R.id.googlePhoto);
        TextView googleName = headerView.findViewById(R.id.googleName);

        // Set thumbnail and name into the view
        String thumbnail = UserData.get("thumbnail", this.getApplicationContext());
        Uri acctPhoto = Uri.parse(thumbnail);
        Glide.with(getApplicationContext()).load(acctPhoto).into(googlePhoto);

        googleName.setText(UserData.get("displayName", this.getApplicationContext()));

        // Setting the CalbiticaCalendar title as the current month, and function of arrow from the layout
        calendar = Calendar.getInstance();
        String currentMonth = DateFormat.getDateInstance(DateFormat.LONG).format(calendar.getTime());
        title = findViewById(R.id.title);
        title.setText(currentMonth.replaceAll("[^a-zA-Z]", "").substring(0, 3) + " " + calendar.get(Calendar.YEAR));

        SimpleDateFormat month_date = new SimpleDateFormat("d/MM/YYYY");
        currentMonth = month_date.format(calendar.getTime());
        final String today = currentMonth;

        // In order to trigger the "Welcome" message first, follow by this...
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(NavigationBar.this, "Today: " + today, Toast.LENGTH_SHORT).show();
            }
        }, 2500);

        arrow = (ImageView) findViewById(R.id.arrow);
        calendarView = (CalendarView) findViewById(R.id.calendarView);
        calendarView.setVisibility(View.GONE);

        arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (arrowTrigger == false) {
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
                title.setText(selectedMonth.substring(0, 3) + " " + calendar.get(Calendar.YEAR));

                String selected = dayOfMonth + "/" + (month + 1) + "/" + year;
                Toast.makeText(NavigationBar.this, "Selected: " + selected, Toast.LENGTH_SHORT).show();

                if (selectedPages == "nav_week") {
                    // As then also pass the data into WeekFragment
                    WeekFragment fragment = WeekFragment.newInstance(calendar.getTime().toString());
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
                }
            }
        });

        new AsyncJob.AsyncJobBuilder<Boolean>().doInBackground(new AsyncJob.AsyncAction<Boolean>() {
            @Override
            public Boolean doAsync() {
                // When open the app, applied all this NavigationBar function into WeekFragment, and return the WeekFragment classes and layout
                if (savedInstanceState == null) {
                    Looper.prepare();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new WeekFragment()).commit();
                    selectedPages = "nav_week";
                    selectedList.add("nav_week");

                    try {
                        navigationView.setCheckedItem(R.id.nav_week);
                    } catch (IllegalStateException e) {
                        System.out.println(e.getLocalizedMessage());
                    }
                }

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }).create().start();
    }

    // Left Navigation Bar Selection
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_week:
                if (!menuItem.isChecked()) {
                    if (WeekFragment.weekMonthCheck) {
                        menuItem.setCheckable(true);

                        // Setting the necessary items for each respective pages
                        arrow.setVisibility(View.VISIBLE);
                        arrowTrigger = false;
                        arrow.setImageResource(R.drawable.down_arrow);
                        calendarView.setVisibility(View.GONE);
                        nav_today.setVisible(true);
                        nav_refresh.setVisible(true);
                        nav_add.setVisible(true);

                        SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
                        String selectedMonth = month_date.format(calendar.getTime());
                        title.setText(selectedMonth.substring(0, 3) + " " + calendar.get(Calendar.YEAR));

                        // addToBackStack(null) -> Allow to go previous page, rather than exit the app
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new WeekFragment()).addToBackStack(null).commit();
                        selectedPages = "nav_week";
                        selectedList.add("nav_week");
                    } else {
                        menuItem.setCheckable(false);
                        Toast.makeText(NavigationBar.this, "Week CalbiticaCalendar still loading, Please Wait...", Toast.LENGTH_SHORT).show();
                    }
                }

                break;
            case R.id.nav_agenda:
                if (!menuItem.isChecked()) {
                    // Setting the necessary items for each respective pages
                    arrow.setVisibility(View.GONE);
                    arrowTrigger = false;
                    arrow.setImageResource(R.drawable.down_arrow);
                    calendarView.setVisibility(View.GONE);
                    nav_today.setVisible(false);
                    nav_refresh.setVisible(true);
                    nav_add.setVisible(true);

                    SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
                    String selectedMonth = month_date.format(calendar.getTime());
                    title.setText(selectedMonth.substring(0, 3) + " " + calendar.get(Calendar.YEAR));

                    // addToBackStack(null) -> Allow to go previous page, rather than exit the app
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AgendaFragment()).addToBackStack(null).commit();
                    selectedPages = "nav_agenda";
                    selectedList.add("nav_agenda");
                }

                break;
            case R.id.sync_calendars:
                if (!menuItem.isChecked()) {
                    // Setting the necessary items for each respective pages
                    title.setText("Sync Calendars");
                    arrow.setVisibility(View.GONE);
                    arrowTrigger = false;
                    arrow.setImageResource(R.drawable.down_arrow);
                    calendarView.setVisibility(View.GONE);
                    nav_today.setVisible(false);
                    nav_refresh.setVisible(false);
                    nav_add.setVisible(false);

                    // addToBackStack(null) -> Allow to go previous page, rather than exit the app
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SyncCalendarsFragment()).addToBackStack(null).commit();
                    selectedPages = "sync_calendars";
                    selectedList.add("sync_calendars");
                }

                break;
            case R.id.nav_profile:
                if (!menuItem.isChecked()) {
                    // Setting the necessary items for each respective pages
                    title.setText("Profile");
                    arrow.setVisibility(View.GONE);
                    arrowTrigger = false;
                    arrow.setImageResource(R.drawable.down_arrow);
                    calendarView.setVisibility(View.GONE);
                    nav_today.setVisible(false);
                    nav_refresh.setVisible(false);
                    nav_add.setVisible(false);

                    // addToBackStack(null) -> Allow to go previous page, rather than exit the app
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).addToBackStack(null).commit();
                    selectedPages = "nav_profile";
                    selectedList.add("nav_profile");
                }

                break;
            case R.id.nav_settings:
                if (!menuItem.isChecked()) {
                    // Setting the necessary items for each respective pages
                    title.setText("Settings");
                    arrow.setVisibility(View.GONE);
                    arrowTrigger = false;
                    arrow.setImageResource(R.drawable.down_arrow);
                    calendarView.setVisibility(View.GONE);
                    nav_today.setVisible(false);
                    nav_refresh.setVisible(false);
                    nav_add.setVisible(false);

                    // addToBackStack(null) -> Allow to go previous page, rather than exit the app
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).addToBackStack(null).commit();
                    selectedPages = "nav_settings";
                    selectedList.add("nav_settings");
                }

                break;
            case R.id.nav_about:
                if (!menuItem.isChecked()) {
                    // Setting the necessary items for each respective pages
                    title.setText("About");
                    arrow.setVisibility(View.GONE);
                    arrowTrigger = false;
                    arrow.setImageResource(R.drawable.down_arrow);
                    calendarView.setVisibility(View.GONE);
                    nav_today.setVisible(false);
                    nav_refresh.setVisible(false);
                    nav_add.setVisible(false);

                    // addToBackStack(null) -> Allow to go previous page, rather than exit the app
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AboutFragment()).addToBackStack(null).commit();
                    selectedPages = "nav_about";
                    selectedList.add("nav_about");
                }

                break;
            case R.id.nav_logout:
                // Logout from Google Account, AND clear shared preferences
                GoogleAuth.getInstance(getApplicationContext()).getClient().signOut();
                UserData.clearAll(getApplicationContext());
                finish(); // finish the current activity

                // direct back to Sign In Activity
                startActivity(new Intent(NavigationBar.this, SignIn.class));
//                finish();
                Toast.makeText(getApplicationContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // handle result of calbit save
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);

        AgendaFragment afInstance = null;
        WeekFragment wfInstance = null;

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (currentFragment instanceof WeekFragment) {
            wfInstance = (WeekFragment) currentFragment;
            CAWrapper.getAllCalbits(getApplicationContext(), wfInstance);
        } else if (currentFragment instanceof AgendaFragment) {
            afInstance = (AgendaFragment) currentFragment;
            CAWrapper.getAllCalbits(getApplicationContext(), afInstance);
        }
    }

    // Right Navigation Bar Creation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.right_navigation_menu, menu);

        // Call the menu layout, to configure the items
        nav_today = menu.findItem(R.id.calendar_today);
        nav_refresh = menu.findItem(R.id.calendar_refresh);
        nav_add = menu.findItem(R.id.calendar_add);

        return true;
    }

    // Right Navigation Bar Selection
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.calendar_today:
                // Set the Week Fragment List as empty then, render from database again, also prevent to spam the button as well
                nav_today.setEnabled(false);

                Calendar today = Calendar.getInstance();

                // Assign to the same calendar to have a link relationship of the Navigation Bar and Today
                calendar.setTime(today.getTime());

                // Title change as well
                SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
                String selectedMonth = month_date.format(calendar.getTime());
                title.setText(selectedMonth.substring(0, 3) + " " + calendar.get(Calendar.YEAR));

                // As then also pass the data into WeekFragment
                WeekFragment fragment = WeekFragment.newInstance(calendar.getTime().toString());
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();

                break;
            case R.id.calendar_refresh:
                Fragment currentFragment = getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);

                if (selectedPages == "nav_week") {
                    // Set the Week Fragment List as empty then, render from database again, also prevent to spam the button as well
                    nav_refresh.setEnabled(false);
                    Toast.makeText(NavigationBar.this, "Retrieving events...", Toast.LENGTH_SHORT)
                            .show();

                    new AsyncJob.AsyncJobBuilder<Boolean>().doInBackground(new AsyncJob.AsyncAction<Boolean>() {
                        @Override
                        public Boolean doAsync() {
                            // Get the event from Calbitica
                            if (currentFragment instanceof WeekFragment) {
                                WeekFragment wfInstance = null;
                                wfInstance = (WeekFragment) currentFragment;
                                CAWrapper.getAllCalbits(getApplicationContext(), wfInstance);
                            }

                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            return true;
                        }
                    })
                            .doWhenFinished(new AsyncJob.AsyncResultAction<Boolean>() {
                                @Override
                                public void onResult(Boolean result) {
                                    // Prevent refresh spamming
                                    nav_refresh.setEnabled(true);
                                }
                            }).create().start();
                } else if (selectedPages == "nav_agenda") {
                    // Set the Agenda Fragment List as empty then, render from database again, also prevent to spam the button as well
                    nav_refresh.setEnabled(false);

                    Toast.makeText(NavigationBar.this, "Retrieving events...", Toast.LENGTH_SHORT)
                            .show();
                    new AsyncJob.AsyncJobBuilder<Boolean>().doInBackground(new AsyncJob.AsyncAction<Boolean>() {
                        @Override
                        public Boolean doAsync() {
                            // Get the event from Calbitica
                            if (currentFragment instanceof AgendaFragment) {
                                AgendaFragment afInstance = null;
                                afInstance = (AgendaFragment) currentFragment;
                                CAWrapper.getAllCalbits(getApplicationContext(), afInstance);
                            }

                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            return true;
                        }
                    })
                            .doWhenFinished(new AsyncJob.AsyncResultAction<Boolean>() {
                                @Override
                                public void onResult(Boolean result) {
                                    // Prevent spamming
                                    nav_refresh.setEnabled(true);
                                }
                            }).create().start();
                }
                break;
            case R.id.calendar_add:
                // Just the empty fields, to give user to key in themselves
                Intent intent = new Intent(NavigationBar.this, WeekSaveEvent.class);

                Bundle data = new Bundle();
                data.putString("startDateTime", "");
                data.putString("endDateTime", "");
                intent.putExtras(data);

                startActivityForResult(intent, 121);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // When pressing back button of mobile will not exit the app, while the nav bar is open
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // To get the previous page, delete the current page
            selectedList.remove(selectedList.size() - 1);

            // When the previous page is the last page, just render the WeekView CalbiticaCalendar(Dashboard/Main Page)
            if (selectedList.size() <= 0) {
                try {
                    AlertDialog.Builder builder = new AlertDialog.Builder(NavigationBar.this);

                    builder.setTitle("Attention!")
                            .setMessage("Are you sure you want to exit the app")
                            // Negative will always on the left, but I prefer in right hand side so...
                            .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            // Positive will always on the right, but I prefer in left hand side so...
                            .setPositiveButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing to go back to the same page
                                }
                            });

                    builder.create().show();

                    // Setting the necessary items for each respective pages
                    arrow.setVisibility(View.VISIBLE);
                    arrowTrigger = false;
                    arrow.setImageResource(R.drawable.down_arrow);
                    calendarView.setVisibility(View.GONE);
                    nav_today.setVisible(true);
                    nav_refresh.setVisible(true);
                    nav_add.setVisible(true);

                    SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
                    String selectedMonth = month_date.format(calendar.getTime());
                    title.setText(selectedMonth.substring(0, 3) + " " + calendar.get(Calendar.YEAR));

                    navigationView.setCheckedItem(R.id.nav_week);

                    // So that it will cause error, due to ArrayList cannot be less than 0, based on default page*
                    selectedList.add("nav_week");
                }catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                super.onBackPressed();
                // ArrayList.get() start from '0' -> Very First Page, ArrayList.size() start from '1' -> Very First Page(Tally both of them)
                selectedPages = selectedList.get(selectedList.size() - 1);

                if (selectedPages.equals("nav_week")) {
                    // Setting the necessary items for each respective pages
                    arrow.setVisibility(View.VISIBLE);
                    arrowTrigger = false;
                    arrow.setImageResource(R.drawable.down_arrow);
                    calendarView.setVisibility(View.GONE);
                    nav_today.setVisible(true);
                    nav_refresh.setVisible(true);
                    nav_add.setVisible(true);

                    SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
                    String selectedMonth = month_date.format(calendar.getTime());
                    title.setText(selectedMonth.substring(0, 3) + " " + calendar.get(Calendar.YEAR));

                    navigationView.setCheckedItem(R.id.nav_week);
                } else if (selectedPages.equals("nav_agenda")) {
                    // Setting the necessary items for each respective pages
                    arrow.setVisibility(View.GONE);
                    arrowTrigger = false;
                    arrow.setImageResource(R.drawable.down_arrow);
                    calendarView.setVisibility(View.GONE);
                    nav_today.setVisible(false);
                    nav_refresh.setVisible(true);
                    nav_add.setVisible(true);

                    SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
                    String selectedMonth = month_date.format(calendar.getTime());
                    title.setText(selectedMonth.substring(0, 3) + " " + calendar.get(Calendar.YEAR));

                    navigationView.setCheckedItem(R.id.nav_agenda);
                } else if (selectedPages.equals("sync_calendars")) {
                    // Setting the necessary items for each respective pages
                    title.setText("Sync Calendars");
                    arrow.setVisibility(View.GONE);
                    arrowTrigger = false;
                    arrow.setImageResource(R.drawable.down_arrow);
                    calendarView.setVisibility(View.GONE);
                    nav_today.setVisible(false);
                    nav_refresh.setVisible(false);
                    nav_add.setVisible(false);

                    navigationView.setCheckedItem(R.id.sync_calendars);
                } else if (selectedPages.equals("nav_profile")) {
                    // Setting the necessary items for each respective pages
                    title.setText("Profile");
                    arrow.setVisibility(View.GONE);
                    arrowTrigger = false;
                    arrow.setImageResource(R.drawable.down_arrow);
                    calendarView.setVisibility(View.GONE);
                    nav_today.setVisible(false);
                    nav_refresh.setVisible(false);
                    nav_add.setVisible(false);

                    navigationView.setCheckedItem(R.id.nav_profile);
                } else if (selectedPages.equals("nav_settings")) {
                    // Setting the necessary items for each respective pages
                    title.setText("Settings");
                    arrow.setVisibility(View.GONE);
                    arrowTrigger = false;
                    arrow.setImageResource(R.drawable.down_arrow);
                    calendarView.setVisibility(View.GONE);
                    nav_today.setVisible(false);
                    nav_refresh.setVisible(false);
                    nav_add.setVisible(false);

                    navigationView.setCheckedItem(R.id.nav_settings);
                } else if (selectedPages.equals("nav_about")) {
                    // Setting the necessary items for each respective pages
                    title.setText("About");
                    arrow.setVisibility(View.GONE);
                    arrowTrigger = false;
                    arrow.setImageResource(R.drawable.down_arrow);
                    calendarView.setVisibility(View.GONE);
                    nav_today.setVisible(false);
                    nav_refresh.setVisible(false);
                    nav_add.setVisible(false);

                    navigationView.setCheckedItem(R.id.nav_about);
                }
            }
        }
    }

    protected void notificationAlert(List<Calbit> calbitList) {
        // Don't do anything if list is empty
        if (calbitList == null || calbitList.size() == 0) return;

        // Clear notifications
        for(PendingIntent p : notifIntents) {
            alarmManager.cancel(p);
            p.cancel();
        }

        notifIntents.clear();

        for (int i = 0; i < calbitList.size(); i++) {
            // To check the current calendar is today, then populate the notification
            Calendar current = Calendar.getInstance();
            current.set(Calendar.SECOND, 0);
            current.set(Calendar.MILLISECOND, 0);
            long currentTimestamp = current.getTime().getTime();

            Calbit currentCalbit = calbitList.get(i);
            if (currentCalbit.getReminders() != null) {

                for (int r = 0; r < currentCalbit.getReminders().size(); r++) {
                    // Using timestamp to check the notification, 100% accurate
                    long reminderTimestamp = currentCalbit.getReminders().get(r).getTime();

                    if (reminderTimestamp > currentTimestamp) {
                        // Calbit currentCalbit = xxx
                        Boolean isAllDay = currentCalbit.getLegitAllDay();

                        String startDateStr = isAllDay
                                ? DateUtil.ddMMMyyyy(currentCalbit.getStart().getDate()) + " at: " + DateUtil.HHmm(currentCalbit.getStart().getDate())
                                : DateUtil.ddMMMyyyy(currentCalbit.getStart().getDateTime()) + " at: " + DateUtil.HHmm(currentCalbit.getStart().getDateTime());

                        String endDateStr = isAllDay
                                ? DateUtil.ddMMMyyyy(currentCalbit.getEnd().getDate()) + " at: " + DateUtil.HHmm(currentCalbit.getEnd().getDate())
                                : DateUtil.ddMMMyyyy(currentCalbit.getEnd().getDateTime()) + " at: " + DateUtil.HHmm(currentCalbit.getEnd().getDateTime());

                        current.setTimeInMillis(reminderTimestamp);

                        notifCount += 1;

                        Intent intent = new Intent(NavigationBar.this, Notification.class);

                        Bundle data = new Bundle();
                        data.putInt("id", notifCount);
                        data.putString("eventName", currentCalbit.getSummary());
                        data.putString("eventStart", startDateStr);
                        data.putString("eventEnd", endDateStr);
                        intent.putExtras(data);

                        PendingIntent pendingIntent = PendingIntent.getBroadcast(NavigationBar.this, notifCount, intent, 0);
                        notifIntents.add(pendingIntent);

                        alarmManager.set(AlarmManager.RTC_WAKEUP, current.getTimeInMillis(), pendingIntent);
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        CAWrapper.getAllCalbits(getApplicationContext(), NavigationBar.this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(UserData.get("jwt", getApplicationContext()) != null) {
            // This data will also get called, if not clearing, it will just creating non-stop
            CAWrapper.getAllCalbits(getApplicationContext(), NavigationBar.this);
        }
    }

    // Check the SDK_INT version to run only on Android 8.0 (API level 26) and higher,
    // Because the notification channels APIs are not available in the support library
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    "Calbitica",
                    "Calbitica",
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }
    }

    // For rendering on Week / Agenda
    // and notifications
    @Override
    public void onCalbitListResult(List<Calbit> calbitList) {
        if (calbitList.isEmpty()) {
            Toast.makeText(NavigationBar.this,
                    "Your session has expired. Please login again.",
                    Toast.LENGTH_SHORT).show();
        } else {
            // run notifications
            notificationAlert(calbitList);
        }
    }

    @Override
    public void onCalbitCompletionFailure() {
        Toast.makeText(NavigationBar.this,
                "Something went wrong. Check your internet connection and try again.",
                Toast.LENGTH_SHORT).show();
    }

}