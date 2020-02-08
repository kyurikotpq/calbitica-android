package com.calbitica.app.SyncCalendars;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.calbitica.app.Database.Database;
import com.calbitica.app.NavigationBar.NavigationBar;
import com.calbitica.app.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import static java.lang.Integer.parseInt;

public class SyncCalendarsFragment extends Fragment {

    public static LinearLayout syncCalendars;          // To show the user the checkbox changes
    public static CheckBox[] checkBox;                 // To plot and add number of checkbox according to the database
    public static String message;                      // To display the sync message status
    public static ProgressDialog progressDialog;       // A fancy loading screen, but it not based the task finish length(Extra Stuff, for fun!)

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
            // A global functions for re-use
            Database database = new Database(getContext());
            database.GetAllCalendars();

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

            // A global functions for re-use
            Database database = new Database(getContext());
            database.UpdateCalendarSyncStatus();
        }
    }
}