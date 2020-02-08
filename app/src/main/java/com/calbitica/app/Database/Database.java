package com.calbitica.app.Database;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;

import com.arasthel.asyncjob.AsyncJob;
import com.calbitica.app.Models.Calendars.Calendar;
import com.calbitica.app.Models.Calendars.Calendars;
import com.calbitica.app.Models.Calendars.SyncCalendar;
import com.calbitica.app.NavigationBar.NavigationBar;
import com.calbitica.app.R;
import com.calbitica.app.SyncCalendars.SyncCalendarsFragment;
import com.calbitica.app.Util.CalbiticaAPI;
import com.calbitica.app.Util.UserData;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.calbitica.app.SyncCalendars.SyncCalendarsFragment.checkBox;
import static com.calbitica.app.SyncCalendars.SyncCalendarsFragment.progressDialog;
import static com.calbitica.app.SyncCalendars.SyncCalendarsFragment.syncCalendars;
import static com.calbitica.app.SyncCalendars.SyncCalendarsFragment.message;

public class Database {
    Context mcontext;
    HashMap<String, String> allCalendars;                   //

    public Database(Context context) {mcontext = context;}

    public Boolean GetAllCalendars() {
        new AsyncJob.AsyncJobBuilder<Boolean>().doInBackground(new AsyncJob.AsyncAction<Boolean>() {
            @Override
            public Boolean doAsync() {
                // Retrieve the JWT
                String jwt = UserData.get("jwt", mcontext);

                // Build the API Call
                Call<Calendars> apiCall = CalbiticaAPI.getInstance(jwt).calendars().getAllCalendars();

                // Make the API Call
                apiCall.enqueue(new Callback<Calendars>() {
                    @Override
                    public void onResponse(Call<Calendars> call, Response<Calendars> response) {
                        if (!response.isSuccessful()) {
                            System.out.println("Unsuccessful to get all Calendars: " + response.code());
                            return;
                        }

                        // Due to the allCalendars parent data require an array with a child method, so have to use loop here...
                        Calendars allCalendars = response.body();

                        try {
                            if (allCalendars.getData() != null) {
                                // Assign the Max Length from the database
                                checkBox = new CheckBox[allCalendars.getData().size()];

                                for(int i = 0; i < allCalendars.getData().size(); i++) {
                                    // Each time will create a new checkbox
                                    checkBox[i] = new CheckBox(mcontext);

                                    checkBox[i].setEnabled(false);

                                    // Checking for the existing Sync Calendars and put a check on the checkbox
                                    if(allCalendars.getData().get(i).getSync()) {
                                        checkBox[i].setChecked(true);
                                    }

                                    checkBox[i].setText(allCalendars.getData().get(i).getSummary());
                                    checkBox[i].setTextColor(mcontext.getResources().getColor(R.color.white));
                                    checkBox[i].setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                                    // Due to the id is "5e381be3e787f0e7afb51d2d", can't really convert it to int
                                    // So put on setHint(String method) to store it...
                                    checkBox[i].setHint(allCalendars.getData().get(i).get_id().toString());

                                    // To display each of the checkbox on the layout
                                    syncCalendars.addView(checkBox[i]);
                                }
                            } else {
                                System.out.println("Ops, Server went wrong, Please try again!");
                            }
                        } catch (Exception e) {
                            System.out.println("Ops the HTTP Request: " + e.getLocalizedMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<Calendars> call, Throwable t) {
                        System.out.println("Fail to get all Calendars: " + t.getMessage());
                    }
                });

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return true;
            }
        }).create().start();

        return false;
    }

    public void UpdateCalendarSyncStatus() {
        if (checkBox != null) {
            // Check all the array that found, and do the sync upon checked...
            for (int i = 0; i < checkBox.length; i++) {
                // Enable back the control to the user
                progressDialog.dismiss();
                checkBox[i].setEnabled(true);
                NavigationBar.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

                checkBox[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        new AsyncJob.AsyncJobBuilder<Boolean>().doInBackground(new AsyncJob.AsyncAction<Boolean>() {
                            @Override
                            public Boolean doAsync() {
                                // Retrieve the JWT
                                String jwt = UserData.get("jwt", mcontext);

                                // Build the API Call, and get the specific checkbox information
                                Call<SyncCalendar> apiCall = CalbiticaAPI.getInstance(jwt).calendars().syncCalendar(buttonView.getHint().toString(), isChecked);

                                // Make the API Call
                                apiCall.enqueue(new Callback<SyncCalendar>() {
                                    @Override
                                    public void onResponse(Call<SyncCalendar> call, Response<SyncCalendar> response) {
                                        if (!response.isSuccessful()) {
                                            System.out.println("Unsuccessful to sync the Calendar: " + response.code());
                                            return;
                                        }

                                        // No need loop, just a simple object and display according to the changes only
                                        SyncCalendar syncCalendar = response.body();
                                        message = syncCalendar.getData().getMessage();
                                    }

                                    @Override
                                    public void onFailure(Call<SyncCalendar> call, Throwable t) {
                                        System.out.println("Fail to sync the Calendar: " + t.getMessage());
                                    }
                                });

                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                return true;
                            }
                        }).doWhenFinished(new AsyncJob.AsyncResultAction<Boolean>() {
                            @Override
                            public void onResult(Boolean result) {
                                Toast.makeText(mcontext, message, Toast.LENGTH_SHORT).show();
                            }
                        }).create().start();
                    }

                });
            }
        } else {
            // Enable back the control to the user
            progressDialog.dismiss();
            NavigationBar.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

            Toast.makeText(mcontext, "There is no data in your database!", Toast.LENGTH_SHORT).show();
        }
    }
}
