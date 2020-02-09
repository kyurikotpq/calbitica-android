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
import com.calbitica.app.NavigationBar.NavigationBar;
import com.calbitica.app.R;
import com.calbitica.app.Week.WeekEditEvent;
import com.calbitica.app.Week.WeekFragment;
import com.calbitica.app.Week.WeekCreateEvent;
import com.github.tibolte.agendacalendarview.AgendaCalendarView;
import com.github.tibolte.agendacalendarview.CalendarPickerController;
import com.github.tibolte.agendacalendarview.models.CalendarEvent;
import com.github.tibolte.agendacalendarview.models.DayItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AgendaFragment extends Fragment{
    public static AgendaCalendarView scheduleView;                      // Mainly modify from the Refresh, etc
    public static List<CalendarEvent> eventList;                        // The events based on Schedule CalbiticaCalendar,
                                                                        // but 1 more phrase on BaseCalendarEvent as a child (From firebase guide)
    public static Calendar minDate, maxDate;                            // Set the necessary fields for the Schedule Fragment
    public static CalendarPickerController calendarPickerController;    // Have to call from here, re-use the same one, rather than keep creating(like a loop)
    private ProgressDialog progressDialog;                              // A fancy loading screen, but it not based the task finish length(Extra Stuff, for fun!)

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_agenda, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // *It is in sequence order
        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute();
    }

    // Here I using two async, due to the default build-in doInBackground cannot allow UI components, which is pain in the ass, so...
    // I get another libraries for the async that allow to run the UI components in doInBackground, make things easier...
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
            // setProgressStyle will change the turning loading(default), to the 0 to 10 loading process
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.show();

            // Prevent the user to press anything
            progressDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            NavigationBar.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            NavigationBar.nav_refresh.setEnabled(false);
            NavigationBar.nav_add.setEnabled(false);
        }

        // (Required)Perform a computation on a background thread, not allow to have UI components(View & void function, etc...)
        @Override
        protected Integer doInBackground(Void... params) {
            scheduleView = getActivity().findViewById(R.id.scheduleView);

            eventList = new ArrayList<>();

            // minimum and maximum date of our calendar
            // 2 year behind, 2 year ahead, example: March 2010 -> Jan 2008 <-> Feb 2012
            minDate = Calendar.getInstance();
            minDate.add(Calendar.YEAR, -2);
            minDate.set(Calendar.DAY_OF_MONTH, 1);

            maxDate = Calendar.getInstance();
            maxDate.add(Calendar.YEAR, 2);
            maxDate.set(Calendar.MONTH, 1);
            maxDate.set(Calendar.DAY_OF_MONTH, 1);

            // Get the event from firebase
            com.calbitica.app.Database.Firebase firebase = new com.calbitica.app.Database.Firebase();
            firebase.getScheduleEventsFromFirebase(eventList);

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

        // Runs on the UI thread after doInBackground, basically is the result of the task
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
            // Due to doInBackground not allow UI components(View), instead doing here then...
            // scheduleView.init -> take quite some time to load, implement loading screen to tell that is not hang/freeze...
            scheduleView.init(eventList, minDate, maxDate, Locale.getDefault(), calendarPickerController = new CalendarPickerController() {
                @Override
                public void onDaySelected(DayItem dayItem) {
                    // When same date pressed, will not be using...
                }

                @Override
                public void onEventSelected(CalendarEvent event) {
                    if (event.getTitle().equals("No events")) {
                        // Re-use the same design & code as the Week CalbiticaCalendar Create Page
                        Intent intent = new Intent(getContext(), WeekCreateEvent.class);
                        Toast.makeText(getActivity(), "No events selected: " + WeekFragment.getEventTitle(event.getInstanceDay()), Toast.LENGTH_SHORT).show();

                        // Set the new event with duration one hour.
                        Calendar endDateTime = (Calendar) event.getInstanceDay().clone();
                        endDateTime.add(Calendar.HOUR, 1);

                        Bundle data = new Bundle();
                        data.putString("startDateTime", event.getInstanceDay().getTime().toString());
                        data.putString("endDateTime", endDateTime.getTime().toString());
                        intent.putExtras(data);

                        startActivity(intent);
                    } else {
                        // Get the layout and render from the CalbiticaCalendar Modal
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        final View mView = getLayoutInflater().inflate(R.layout.calendar_modal, null);

                        CheckBox check = (CheckBox) mView.findViewById(R.id.calendar_Modal_eventCheckBox);
                        TextView title = (TextView) mView.findViewById(R.id.calendar_Modal_eventTitle);
                        TextView startDateTime = (TextView) mView.findViewById(R.id.calendar_Modal_eventStartDateTime);
                        TextView endDateTime = (TextView) mView.findViewById(R.id.calendar_Modal_eventEndDateTime);
                        ImageView editing = (ImageView) mView.findViewById(R.id.calendar_Modal_editing);
                        ImageView deleting = (ImageView) mView.findViewById(R.id.calendar_Modal_deleting);
                        ImageView close = (ImageView) mView.findViewById(R.id.calendar_Modal_eventClose);

                        title.setText(event.getTitle());

                        // Convert to our respective  datetime format of start and end DateTime
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
                        dialog.show();

                        check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if(isChecked) {
                                    Toast.makeText(getActivity(), "Completed the task and earn the exp points", Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                        editing.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Using back the same design as the WeekEditEvent
                                Intent intent = new Intent(getContext(), WeekEditEvent.class);

                                Bundle calendarData = new Bundle();
                                calendarData.putLong("id", event.getId());
                                calendarData.putString("title", event.getTitle());
                                calendarData.putString("startDateTime", event.getStartTime().getTime().toString());
                                calendarData.putString("endDateTime", event.getEndTime().getTime().toString());

                                // Schedule CalbiticaCalendar did not provide the color here, so have to get the color from database instead...
                                DatabaseReference firebase = FirebaseDatabase.getInstance().getReference().child(NavigationBar.acctName).child("Calbitica").child("CalbiticaCalendar");
                                firebase.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            Map<String, String> eventData = (Map<String, String>) dataSnapshot.getValue();
                                            for (String key : eventData.keySet()) {
                                                Object data = eventData.get(key);

                                                try {
                                                    HashMap<String, Object> eventObject = (HashMap<String, Object>) data;

                                                    // Making use of calendarID to do the checking(Unique id also)
                                                    long eventID = (long) eventObject.get("calendarID");

                                                    // Check and render the firebase with the existing data(Only 1 data will be found)
                                                    if (eventID == event.getId()) {
                                                        // Getting the JSON Object from colorInfo, based on key
                                                        HashMap<String, Object> colorObject = (HashMap<String, Object>) eventObject.get("colorInfo");

                                                        // Check and render the firebase with the existing data(Only 1 data will be found)
                                                        String firebaseEvent = colorObject.get("color").toString();
                                                        int firebaseColor = Integer.parseInt(firebaseEvent);

                                                        calendarData.putInt("color", firebaseColor);
                                                        intent.putExtras(calendarData);

                                                        startActivity(intent);
                                                        dialog.dismiss();
                                                    }
                                                } catch (ClassCastException cce) {
                                                    // If the object can’t be casted into HashMap, it means that it is of type String.
                                                    try {
                                                        String mString = String.valueOf(eventData.get(key));
                                                        System.out.println("data mString " + mString);
                                                    } catch (ClassCastException cce2) {
                                                        cce2.printStackTrace();
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        System.out.println(databaseError.getCode());
                                    }
                                });
                            }
                        });

                        deleting.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Delete from Firebase with the existing data
                                com.calbitica.app.Database.Firebase firebase = new com.calbitica.app.Database.Firebase();
                                firebase.deleteWeekEventFromFirebase(event.getId());

                                // Delete event with existing data
                                for (int i = 0; i < eventList.size(); i++) {
                                    if(eventList.get(i).getId() == event.getId()) {
                                        AgendaFragment.eventList.remove(i);   // remove only 1

                                        // Schedule CalbiticaCalendar will also re-render the events as well
                                        AgendaFragment.scheduleView.init(AgendaFragment.eventList, AgendaFragment.minDate, AgendaFragment.maxDate, Locale.getDefault(), AgendaFragment.calendarPickerController);
                                    }
                                }

                                // Schedule CalbiticaCalendar will also re-render the events as well
                                scheduleView.init(eventList, minDate, maxDate, Locale.getDefault(), calendarPickerController);
                                Toast.makeText(getActivity(),"Event successfully deleted", Toast.LENGTH_SHORT).show();
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
                }

                @Override
                public void onScrollToDate(Calendar calendar) {
                    // When selected different date or scroll the date, it will change the Navigation Bar of the Title
                    String currentMonth = DateFormat.getDateInstance(DateFormat.LONG).format(calendar.getTime());
                    NavigationBar.title.setText(currentMonth.replaceAll("[^a-zA-Z]", "").substring(0, 3) + " "  + calendar.get(Calendar.YEAR));
                }
            });

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

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
}