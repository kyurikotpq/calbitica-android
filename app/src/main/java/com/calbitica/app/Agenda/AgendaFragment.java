package com.calbitica.app.Agenda;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
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
import com.calbitica.app.Models.Calbit.Calbit;
import com.calbitica.app.Models.Calbit.TaskCompleted;
import com.calbitica.app.NavigationBar.NavigationBar;
import com.calbitica.app.R;
import com.calbitica.app.Util.CAWrapper;
import com.calbitica.app.Util.DateUtil;
import com.calbitica.app.Util.CalbitResultInterface;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_agenda, container, false);
    }

    // handle result of calbit save
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case (122): {
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    CAWrapper.getAllCalbits(
                            getActivity().getApplicationContext(),
                            AgendaFragment.this
                    );
                }
                break;
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Get the events from Calbitica
        calendarPickerController = new CalendarPickerController() {
            @Override
            public void onDaySelected(DayItem dayItem) {
                // We need to implement this method due to the nature of the class,
                // but we're not using it... so cher don't penalize us please
            }

            @Override
            public void onEventSelected(CalendarEvent currentCalendarEvent) {
                BaseCalendarEvent event = (BaseCalendarEvent) currentCalendarEvent;

                // Create a new event
                if (event.getTitle().equals("No events")) {
                    // Re-use the same design & code as the Week CalbiticaCalendar Create Page
                    Intent intent = new Intent(getContext(), WeekSaveEvent.class);

                    // Set the new event with duration 30mins.
                    Calendar endDateTime = (Calendar) event.getInstanceDay().clone();
                    endDateTime.add(Calendar.MINUTE, 30);

                    Bundle data = new Bundle();

                    data.putString("startDateTime", DateUtil.localToUTC(event.getInstanceDay().getTime()));
                    data.putString("endDateTime", DateUtil.localToUTC(endDateTime.getTime()));

                    intent.putExtras(data);

                    startActivityForResult(intent, 122);
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
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, YYYY HH:mm", Locale.ENGLISH);
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
                    Calbit currentCalbit = listOfCalbits.get(currentAgendaIndex);

                    new AsyncJob.AsyncJobBuilder<Boolean>()
                            .doInBackground(new AsyncJob.AsyncAction<Boolean>() {
                                @Override
                                public Boolean doAsync() {
                                    if (currentCalbit.get_id() != null) {
                                        currentSelectedMongoID = currentCalbit.get_id().toString();
                                    }

                                    mongoReminder = (currentCalbit.getReminders() != null
                                            && currentCalbit.getReminders().size() > 0)
                                            ? DateUtil.localToUTC(currentCalbit.getReminders().get(0))
                                            : "";

                                    // To allow to run Toast in the async method...
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (currentCalbit.getCompleted() != null) {
                                                check.setChecked(
                                                        currentCalbit.getCompleted().getStatus()
                                                );
                                            }
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
                                            CAWrapper.updateCalbitCompletion(getActivity().getApplicationContext(),
                                                    currentSelectedMongoID, completion,
                                                    AgendaFragment.this);

                                            // Update the local copy
                                            // We will do local updates here, regardless of the result of the API call,
                                            // as sooner or later the real changes will show up.
                                            int newColor = (isChecked) ? R.color.gray_3 : R.color.blue_3;
                                            event.setColor(getResources().getColor(newColor, null));
                                            eventList.set(currentAgendaIndex, event);

                                            // update on the list of calbits too
                                            currentCalbit.setCompleted(new TaskCompleted(isChecked));
                                            listOfCalbits.set(currentAgendaIndex, currentCalbit);

                                            if (isChecked) {
                                                dialog.dismiss();
                                            }
                                            agendaView.init(eventList, minDate, maxDate,
                                                    Locale.getDefault(), calendarPickerController);

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
                                    calendarData.putString("calendarID", currentCalbit.getCalendarID());

                                    calendarData.putString("startDateTime",
                                            DateUtil.localToUTC(event.getStartTime().getTime()));
                                    calendarData.putString("endDateTime",
                                            DateUtil.localToUTC(event.getEndTime().getTime()));

                                    calendarData.putString("reminderDateTime", mongoReminder);
                                    calendarData.putBoolean("legitAllDay", currentCalbit.getAllDay());
                                    calendarData.putString("googleID", currentCalbit.getGoogleID());

                                    intent.putExtras(calendarData);

                                    startActivityForResult(intent, 122);
                                    dialog.dismiss();
                                }
                            });

                            deleting.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // Delete from Calbitica with the existing data
                                    CAWrapper.deleteCalbit(getActivity().getApplicationContext(), currentSelectedMongoID,
                                            AgendaFragment.this);

                                    // Delete event with existing data
                                    for (int i = 0; i < eventList.size(); i++) {
                                        if (eventList.get(i).getId() == event.getId()) {
                                            eventList.remove(i); // remove only 1
                                            listOfCalbits.removeIf(c -> c.get_id().equals(currentSelectedMongoID));
                                        }
                                    }

                                    // reset indices
                                    for (int i = 0; i < eventList.size(); i++) {
                                        eventList.get(i).setId(i);
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
        CAWrapper.getAllCalbits(getActivity().getApplicationContext(), AgendaFragment.this);

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
            try {
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
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        // Runs on the UI thread after doInBackground, basically is the result of the
        // task
        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
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
            agendaView.init(eventList, minDate, maxDate, Locale.getDefault(),
                    calendarPickerController);

            AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                @Override
                public void doInUIThread() {
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

        for (int i = 0; i < calbitList.size(); i++) {
            Calbit currentCalbit = calbitList.get(i);

            // Declare the necessary fields into Week View Calendar
            Calendar startDateTime = Calendar.getInstance();
            Calendar endDateTime = Calendar.getInstance();

            try {
                Date startDateObj = currentCalbit.getLegitAllDay()
                        ? currentCalbit.getStart().getDate()
                        : currentCalbit.getStart().getDateTime();

                startDateTime.setTime(startDateObj);

                Date endDateObj = currentCalbit.getLegitAllDay()
                        ? currentCalbit.getEnd().getDate()
                        : currentCalbit.getEnd().getDateTime();
                endDateTime.setTime(endDateObj);

            } catch (Exception e) {
                e.printStackTrace();
            }

            // Based on the Agenda Calendar format, and return back the list
            // Auto-configure the task completion of color and checked according to calbitica
            try {
                int newColor = (currentCalbit.getCompleted().getStatus()) ? R.color.gray_3 : R.color.blue_3;
                int resourceColor = getActivity().getResources().getColor(newColor, null);

                BaseCalendarEvent allEvent = new BaseCalendarEvent(
                        currentCalbit.getSummary(), "", "",
                        resourceColor, startDateTime, endDateTime, currentCalbit.getAllDay());

                allEvent.setId(i);
                eventList.add(allEvent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Refresh the agenda calendar view
        listOfCalbits = calbitList;

        try {
            agendaView.init(eventList, minDate, maxDate, Locale.getDefault(), calendarPickerController);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCalbitCompletionFailure() {
        Toast.makeText(getContext(), "That action didn't go through; please try again.", Toast.LENGTH_SHORT).show();
    }
}