package com.calbitica.app.SyncCalendars;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.arasthel.asyncjob.AsyncJob;
import com.calbitica.app.Models.Calendars.Calendars;
import com.calbitica.app.Models.Calendars.SyncCalendar;
import com.calbitica.app.NavigationBar.NavigationBar;
import com.calbitica.app.R;
import com.calbitica.app.Util.CalbiticaAPI;
import com.calbitica.app.Util.UserData;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static java.lang.Integer.parseInt;

public class SyncCalendarsFragment extends Fragment {

    public LinearLayout syncCalendars;          // To show the user the checkbox changes
    public CheckBox[] checkBox;                 // To plot and add number of checkbox according to the database
    public String message;                      // To display the sync message status
    public ProgressDialog progressDialog;       // A fancy loading screen, but it not based the task finish length(Extra Stuff, for fun!)

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sync_calendars, container, false);   //Inflate Layout
        return view;    //return default view
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        syncCalendars = getActivity().findViewById(R.id.sync_calendars_layout);

        // *It is in sequence order
        SyncCalendarsFragment.AsyncTaskRunner runner = new SyncCalendarsFragment.AsyncTaskRunner();
        runner.execute();
    }

    private class AsyncTaskRunner extends AsyncTask<Void, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            // Just the fancy loading screen here...
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Loading in Progress...");
            // Range of 0 to 10, for the ProgressDialog(Loading)
            progressDialog.setProgress(0);
            progressDialog.setMax(5);
            // setProgressStyle will change the turning loading(default), to the 0 to 10 loading process
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.show();

            // Prevent the user to press anything
            progressDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            NavigationBar.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            new AsyncJob.AsyncJobBuilder<Boolean>().doInBackground(new AsyncJob.AsyncAction<Boolean>() {
                @Override
                public Boolean doAsync() {
                    // Retrieve the JWT
                    String jwt = UserData.get("jwt", getContext());

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
                                        checkBox[i] = new CheckBox(getContext());

                                        checkBox[i].setEnabled(false);

                                        // Checking for the existing Sync Calendars and put a check on the checkbox
                                        if(allCalendars.getData().get(i).getSync()) {
                                            checkBox[i].setChecked(true);
                                        }

                                        checkBox[i].setText(allCalendars.getData().get(i).getSummary());
                                        checkBox[i].setTextColor(getContext().getResources().getColor(R.color.white));
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

            // This will populate the progressDialog
            for (int count = 0; count < 6; count++) {
                // Each time will load and update the progress accordingly
                progressDialog.setProgress(count);

                try {
                    // need to wait, in order to show the progressDialog
                    Thread.sleep(200);
                    // onProgressUpdate will be showing in process
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

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
                                    String jwt = UserData.get("jwt", getContext());

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
                                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                }
                            }).create().start();
                        }

                    });
                }
            } else {
                // Enable back the control to the user
                progressDialog.dismiss();
                NavigationBar.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

                Toast.makeText(getContext(), "There is no data in your database!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}