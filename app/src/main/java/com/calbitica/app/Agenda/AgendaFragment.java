package com.calbitica.app.Agenda;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.arasthel.asyncjob.AsyncJob;
import com.calbitica.app.Database.Database;
import com.calbitica.app.Models.Calbit.Calbit;
import com.calbitica.app.NavigationBar.NavigationBar;
import com.calbitica.app.R;
import com.calbitica.app.Util.CAWrapper;
import com.calbitica.app.Week.CalbitResultInterface;
import com.calbitica.app.Week.WeekEditEvent;
import com.calbitica.app.Week.WeekFragment;
import com.calbitica.app.Week.WeekSaveEvent;
import com.github.tibolte.agendacalendarview.AgendaCalendarView;
import com.github.tibolte.agendacalendarview.CalendarPickerController;
import com.github.tibolte.agendacalendarview.models.BaseCalendarEvent;
import com.github.tibolte.agendacalendarview.models.CalendarEvent;
import com.github.tibolte.agendacalendarview.models.DayItem;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.calbitica.app.Week.WeekFragment.listOfCalbits;

public class AgendaFragment extends Fragment implements CalbitResultInterface {
    public static AgendaCalendarView agendaView; // Mainly modify from the Refresh, etc
    public static List<CalendarEvent> eventList; // The events based on Agenda Calendar, but 1 more phrase on
    // BaseCalendarEvent as a child
    public static List<Calbit> listOfCalbits;               // Temp storage of calbit list from API

    public static Calendar minDate, maxDate; // Set the necessary fields for the Agenda Fragment
    public static CalendarPickerController calendarPickerController; // Have to call from here, re-use the same one,
    // rather than keep creating(like a loop)
    private ProgressDialog progressDialog; // A fancy loading screen, but it not based the task finish length(Extra
    // Stuff, for fun!)

    private String currentSelectedMongoID = null; // Particular events(Mainly for edit and delete)
    private String mongoReminder = null; // Particular events(Pass the bundle to the edit event)

    boolean firstLoad = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_agenda, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        firstLoad = true;

        // *It is in sequence order
        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute();
    }

    // Here I using two async, due to the default build-in doInBackground cannot
    // allow UI components, which is pain in the ass, so...
    // I get another libraries for the async that allow to run the UI components in
    // doInBackground, make things easier...
    private class AsyncTaskRunner extends AsyncTask<Void, Integer, Integer> {
        // Runs on the UI thread before doInBackground
        @Override
        protected void onPreExecute() {
            // Just the fancy loading screen here...
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Loading in Progress...");
            // Range of 0 to 10, for the ProgressDialog(Loading)
            progressDialog.setProgress(0);
            progressDialog.setMax(5);
            // setProgressStyle will change the turning loading(default), to the 0 to 10
            // loading process
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.show();

            // Prevent the user to press anything
            progressDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            NavigationBar.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            NavigationBar.nav_refresh.setEnabled(false);
            NavigationBar.nav_add.setEnabled(false);
        }

        // (Required)Perform a computation on a background thread, not allow to have UI
        // components(View & void function, etc...)
        @Override
        protected Integer doInBackground(Void... params) {
            agendaView = getActivity().findViewById(R.id.agendaView);

            eventList = new ArrayList<>();
            listOfCalbits = new ArrayList<>();

            // minimum and maximum date of our calendar
            // 2 year behind, 2 year ahead, example: March 2010 -> Jan 2008 <-> Feb 2012
            minDate = Calendar.getInstance();
            minDate.add(Calendar.YEAR, -2);
            minDate.set(Calendar.DAY_OF_MONTH, 1);

            maxDate = Calendar.getInstance();
            maxDate.add(Calendar.YEAR, 2);
            maxDate.set(Calendar.MONTH, 1);
            maxDate.set(Calendar.DAY_OF_MONTH, 1);

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

        // Runs on the UI thread after doInBackground, basically is the result of the
        // task
        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            // Here is another async...
            job.doOnBackground();
        }
    }

    AsyncJob.OnBackgroundJob job = new AsyncJob.OnBackgroundJob() {
        @Override
        public void doOnBackground() {
            // Due to doInBackground not allow UI components(View), instead doing here
            // then...
            // agendaView.init -> take quite some time to load, implement loading screen to
            // tell that is not hang/freeze...
            calendarPickerController = new CalendarPickerController() {
                @Override
                public void onDaySelected(DayItem dayItem) {
                    // We need to implement this method due to the nature of the class,
                    // but we're not using it... so cher don't penalize us please
                }

                @Override
                public void onEventSelected(CalendarEvent event) {
                    // Create a new event
                    if (event.getTitle().equals("No events")) {
                        // Re-use the same design & code as the Week CalbiticaCalendar Create Page
                        Intent intent = new Intent(getContext(), WeekSaveEvent.class);

                        // Set the new event with duration 30mins.
                        Calendar endDateTime = (Calendar) event.getInstanceDay().clone();
                        endDateTime.add(Calendar.MINUTE, 30);

                        Bundle data = new Bundle();
                        data.putString("startDateTime", event.getInstanceDay().getTime().toString());
                        data.putString("endDateTime", endDateTime.getTime().toString());
                        intent.putExtras(data);

                        startActivity(intent);
                    } else {
                        // Viewing event detail
                        // Get the layout and render from the Calendar Modal
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        final View mView = getLayoutInflater().inflate(R.layout.calendar_modal, null);

                        CheckBox check = (CheckBox) mView.findViewById(R.id.calendar_Modal_eventCheckBox);
                        TextView title = (TextView) mView.findViewById(R.id.calendar_Modal_eventTitle);
                        TextView startDateTime = (TextView) mView
                                .findViewById(R.id.calendar_Modal_eventStartDateTime);
                        TextView endDateTime = (TextView) mView
                                .findViewById(R.id.calendar_Modal_eventEndDateTime);
                        ImageView editing = (ImageView) mView.findViewById(R.id.calendar_Modal_editing);
                        ImageView deleting = (ImageView) mView.findViewById(R.id.calendar_Modal_deleting);
                        ImageView close = (ImageView) mView.findViewById(R.id.calendar_Modal_eventClose);

                        title.setText(event.getTitle());

                        // Convert to our respective datetime format of start and end DateTime
                        Timestamp startTimeStamp = new Timestamp(event.getStartTime().getTimeInMillis());
                        Timestamp endTimeStamp = new Timestamp(event.getEndTime().getTimeInMillis());
                        SimpleDateFormat sdf = new SimpleDateFormat("MMM D, YYYY HH:mm", Locale.ENGLISH);
                        String start = sdf.format(startTimeStamp);
                        String end = sdf.format(endTimeStamp);

                        startDateTime.setText("\n" + start + " - ");
                        endDateTime.setText("\n" + end);

                        // Render the Modal, must be in the last of the code
                        builder.setView(mView);
                        final AlertDialog dialog = builder.create();

                        try {
                            dialog.show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // Setting the valid currentSelectedMongoID for the reference with the database
                        int currentAgendaIndex = (int) event.getId();

                        new AsyncJob.AsyncJobBuilder<Boolean>()
                                .doInBackground(new AsyncJob.AsyncAction<Boolean>() {
                                    @Override
                                    public Boolean doAsync() {
                                        Calbit currentCalbit = listOfCalbits.get(currentAgendaIndex);
                                        if (currentCalbit.get_id() != null) {
                                            currentSelectedMongoID = currentCalbit.get_id().toString();
                                        }

                                        mongoReminder = (currentCalbit.getReminders() != null)
                                                ? currentCalbit.getReminders().toString()
                                                : "";

                                        if (currentCalbit.getCompleted() != null) {
                                            check.setChecked(
                                                    currentCalbit.getCompleted().getStatus()
                                            );
                                        }

                                        // To allow to run Toast in the async method...
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                check.setEnabled(false);
                                                Toast.makeText(getContext(), "Please wait...",
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        });

                                        try {
                                            Thread.sleep(3000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }

                                        return true;
                                    }
                                }).doWhenFinished(new AsyncJob.AsyncResultAction<Boolean>() {
                            @Override
                            public void onResult(Boolean result) {
                                Toast.makeText(getContext(), "Done!", Toast.LENGTH_SHORT).show();
                                check.setEnabled(true);

                                check.setOnCheckedChangeListener(
                                        new CompoundButton.OnCheckedChangeListener() {
                                            @Override
                                            public void onCheckedChanged(CompoundButton buttonView,
                                                                         boolean isChecked) {
                                                // Update the database
                                                HashMap<String, Boolean> completion = new HashMap<>();
                                                completion.put("status", isChecked);
                                                CAWrapper.updateCalbitCompletion(getContext(),
                                                        currentSelectedMongoID, completion,
                                                        AgendaFragment.this);

                                                if (isChecked) {
                                                    dialog.dismiss();

                                                    // Refresh the fragment again, to populate changes
                                                    getActivity().getSupportFragmentManager()
                                                            .beginTransaction()
                                                            .detach(AgendaFragment.this)
                                                            .attach(AgendaFragment.this).commit();
                                                }
                                            }
                                        });

                                editing.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        // Using back the same design as the WeekEditEvent
                                        Intent intent = new Intent(getContext(), WeekSaveEvent.class);

                                        Bundle calendarData = new Bundle();
                                        calendarData.putString("id", currentSelectedMongoID);
                                        calendarData.putString("title", event.getTitle());
                                        calendarData.putString("startDateTime",
                                                event.getStartTime().getTime().toString());
                                        calendarData.putString("endDateTime",
                                                event.getEndTime().getTime().toString());
                                        calendarData.putString("reminderDateTime", mongoReminder);
                                        intent.putExtras(calendarData);

                                        startActivity(intent);
                                        dialog.dismiss();
                                    }
                                });

                                deleting.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        // Delete from Calbitica with the existing data
                                        CAWrapper.deleteCalbit(getContext(), currentSelectedMongoID,
                                                AgendaFragment.this);

                                        // Delete event with existing data
                                        for (int i = 0; i < eventList.size(); i++) {
                                            if (eventList.get(i).getId() == event.getId()) {
                                                eventList.remove(i); // remove only 1
                                                listOfCalbits.removeIf(c -> c.get_id().equals(currentSelectedMongoID));
                                            }
                                        }

                                        // Refresh the agenda calendar view
                                        agendaView.init(eventList, minDate, maxDate,
                                                Locale.getDefault(), calendarPickerController);
                                        Toast.makeText(getActivity(), "Event successfully deleted",
                                                Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                });

                                close.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                    }
                                });
                            }
                        }).create().start();
                    }
                }

                @Override
                public void onScrollToDate(Calendar calendar) {
                    // When selected different date or scroll the date, it will change the
                    // Navigation Bar of the Title
                    String currentMonth = DateFormat.getDateInstance(DateFormat.LONG)
                            .format(calendar.getTime());
                    NavigationBar.title.setText(currentMonth.replaceAll("[^a-zA-Z]", "").substring(0, 3) + " "
                            + calendar.get(Calendar.YEAR));
                }
            };

            // Get the events from Calbitica
            CAWrapper.getAllCalbits(getContext(), AgendaFragment.this);

            agendaView.init(eventList, minDate, maxDate, Locale.getDefault(),
                    calendarPickerController);

            try {
                Thread.sleep(1000);
            } catch (
                    InterruptedException e) {
                e.printStackTrace();
            }

            AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                @Override
                public void doInUIThread() {
                    firstLoad = false;

                    // Enable back the control to the user
                    progressDialog.dismiss();
                    NavigationBar.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    NavigationBar.nav_refresh.setEnabled(true);
                    NavigationBar.nav_add.setEnabled(true);
                }
            });
        }
    };


    // API handlers
    @Override
    public void onCalbitListResult(List<Calbit> calbitList) {
        listOfCalbits.clear();
        eventList.clear();

        for (int i = 0; i < listOfCalbits.size(); i++) {
            Calbit currentCalbit = listOfCalbits.get(i);

            // Declare the necessary fields into Week View Calendar
            java.util.Calendar startDateTime = java.util.Calendar.getInstance();
            java.util.Calendar endDateTime = java.util.Calendar.getInstance();

            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);

            try {
                Date startDateObj = currentCalbit.getLegitAllDay()
                        ? sdf.parse(currentCalbit.getStart().getDate().toString())
                        : sdf.parse(currentCalbit.getStart().getDateTime().toString());
                startDateTime.setTime(startDateObj);

                Date endDateObj = currentCalbit.getLegitAllDay()
                        ? sdf.parse(currentCalbit.getEnd().getDate().toString())
                        : sdf.parse(currentCalbit.getEnd().getDateTime().toString());
                endDateTime.setTime(endDateObj);

            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }

            // Based on the Agenda Calendar format, and return back the list
            int newColor = (currentCalbit.getCompleted().getStatus()) ? R.color.gray_3 : R.color.blue_3;

            BaseCalendarEvent allEvent = new BaseCalendarEvent(
                    currentCalbit.getSummary(), "", "",
                    newColor, startDateTime, endDateTime, currentCalbit.getAllDay());

            // Auto-configure the task completion of color and checked according to calbitica
            // Can't do the checkbox here accordingly, due to this functions should not indirect any
            // of the components in the WeekFragment, have to check separately
            // Overwrite the previous color, due to required fields from libraries
//            if (currentCalbit.getCompleted() != null) {
//                if (currentCalbit.getCompleted().getStatus()) {
//                    allEvent.setColor(Color.rgb(200, 200, 200));
//                } else {
//                    allEvent.setColor(Color.rgb(100, 200, 220));
//                }
//            }
            allEvent.setId(i);

            eventList.add(allEvent);
        }
        // Refresh the agenda calendar view
        listOfCalbits = calbitList;

        if(!firstLoad) {
            agendaView.init(eventList, minDate, maxDate,
                    Locale.getDefault(), calendarPickerController);
        }
    }

    @Override
    public void onCalbitCompletionFailure() {
        Toast.makeText(getContext(), "That action didn't go through; please try again.", Toast.LENGTH_SHORT).show();
    }
}