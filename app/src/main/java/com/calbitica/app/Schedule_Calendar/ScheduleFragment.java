package com.calbitica.app.Schedule_Calendar;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.calbitica.app.R;
import com.github.tibolte.agendacalendarview.AgendaCalendarView;
import com.github.tibolte.agendacalendarview.CalendarPickerController;
import com.github.tibolte.agendacalendarview.models.CalendarEvent;
import com.github.tibolte.agendacalendarview.models.DayItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ScheduleFragment extends Fragment{
    AgendaCalendarView scheduleView;
    List<CalendarEvent> eventList;
    Calendar minDate, maxDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // URL Request links
        OkHttpClient client = new OkHttpClient();
        String url = "https://reqres.in/api/users?page=2";

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()) {
                    final String myResponse = response.body().string();

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("myResponse " + myResponse);
                        }
                    });
                }
            }
        });

        // *It is in sequence order
        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute();
    }

    private class AsyncTaskRunner extends AsyncTask<Void, Integer, String> {
        ProgressDialog progressDialog;

        // Runs on the UI thread before doInBackground
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Loading in Progress...");
            // Range of 0 to 10, for the ProgressDialog(Loading)
            progressDialog.setProgress(0);
            progressDialog.setMax(10);
            // setProgressStyle will change the turning loading(default), to the 0 to 10 loading process
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.show();
        }

        // (Required)Perform a computation on a background thread, not allow to have UI components(View & void function, etc...)
        @Override
        protected String doInBackground(Void... params) {
            scheduleView = getActivity().findViewById(R.id.scheduleView);

            // minimum and maximum date of our calendar
            // 2 year behind, 2 year ahead, example: March 2010 -> Jan 2008 <-> Feb 2012
            minDate = Calendar.getInstance();
            minDate.add(Calendar.YEAR, -2);
            minDate.set(Calendar.DAY_OF_MONTH, 1);

            maxDate = Calendar.getInstance();
            maxDate.add(Calendar.YEAR, 2);
            maxDate.set(Calendar.MONTH, 1);
            maxDate.set(Calendar.DAY_OF_MONTH, 1);

            eventList = new ArrayList<>();
            // Get the event from firebase
            com.calbitica.app.Database.Firebase firebase = new com.calbitica.app.Database.Firebase();
            firebase.getScheduleEventsFromFirebase(eventList);

            // This will populate the progressDialog
            int i = 0;
            synchronized (this) {
                while (i < 11) {
                    try {
                        // need to wait, in order to show the progressDialog
                        wait(100);
                        i++;
                        publishProgress(i);
                        // onProgressUpdate will be showing in process
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            return "Loading Complete...";
        }

        // Runs on the UI thread after publishProgress(Progress...)(Must Have!), during the process of task
        @Override
        protected void onProgressUpdate(Integer... values) {
            // Each time will load and update the progress accordingly
            int progress = values[0];
            progressDialog.setProgress(progress);
        }

        // Runs on the UI thread after doInBackground, basically is the result of the task
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Due to doInBackground not allow UI components(View), instead doing here then...
            // scheduleView.init -> take quite some time to load, implement loading screen to tell that is not hang/freeze...
            scheduleView.init(eventList, minDate, maxDate, Locale.getDefault(), new CalendarPickerController() {
                @Override
                public void onDaySelected(DayItem dayItem) {
                    Toast.makeText(getActivity(), "dayItem " + dayItem, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onEventSelected(CalendarEvent event) {
                    Toast.makeText(getActivity(), "event " + event, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onScrollToDate(Calendar calendar) {
                    Toast.makeText(getActivity(), "calendar " + calendar.getTime(), Toast.LENGTH_SHORT).show();
                }
            });

            progressDialog.dismiss();
        }
    }
}