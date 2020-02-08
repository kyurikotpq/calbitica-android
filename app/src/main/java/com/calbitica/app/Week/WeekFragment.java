package com.calbitica.app.Week;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.arasthel.asyncjob.AsyncJob;
import com.calbitica.app.Database.Database;
import com.calbitica.app.Models.Calbit.Calbit;
import com.calbitica.app.NavigationBar.NavigationBar;
import com.calbitica.app.R;

public class WeekFragment extends Fragment {
    public static WeekView weekView;                                    // Mostly used from NavigationBar refresh, etc...(Week Calender)
    public static ArrayList<WeekViewEvent> mNewEvents;                  // Mostly used from NavigationBar refresh, etc...(Event in Week Calendar)
    public static boolean weekMonthCheck;                               // Ensure the weekView is loaded finished
    public static List<Calbit> listOfCalbits;                           // Temp storage of calbit list from API
    private String mongoId = null;                                      // Particular events(Mainly for edit and delete)
    private String mongoReminder = null;                                // Particular events(Pass the bundle to the edit event)

    public static WeekFragment newInstance(String selectedDate) {
        WeekFragment fragment = new WeekFragment();
        Bundle data = new Bundle();
        data.putString("selectedDate", selectedDate);
        fragment.setArguments(data);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_week_calendar, container, false);//Inflate Layout
        return view;    //return default view
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        new AsyncJob.AsyncJobBuilder<Boolean>().doInBackground(new AsyncJob.AsyncAction<Boolean>() {
            @Override
            public Boolean doAsync() {
                weekMonthCheck = false;
                weekView = getActivity().findViewById(R.id.weekView);
                mNewEvents = new ArrayList<WeekViewEvent>();

                // Get the information from the getCalendarMonths(required)
                weekView.setMonthChangeListener(new MonthLoader.MonthChangeListener() {
                    @Override
                    public List<WeekViewEvent> onMonthChange(int newYear, int newMonth) {
                        // Populate the week view with the events
                        List<WeekViewEvent> events = new ArrayList<WeekViewEvent>();
                        ArrayList<WeekViewEvent> newEvents = getCalendarMonths(newYear, newMonth);
                        events.addAll(newEvents);
                        return events;
                    }
                });

                // Set up a date time interpreter to interpret how the date and time will be formatted in
                // the week view. This is optional.
                setupDateTimeInterpreter(true);

                // Get the event from Calbitica
                Database database = new Database(getContext());
                database.getAllCalbitAndRenderOnWeek();

                return true;
            }
        }).doWhenFinished(new AsyncJob.AsyncResultAction<Boolean>() {
            @Override
            public void onResult(Boolean  result) {
                // Get the _id from the database, as for valid checking
                Database database = new Database(getContext());
                database.getAllCalbit();

                // When click on the empty event(Will be the creating event)
                weekView.setEmptyViewClickListener(new WeekView.EmptyViewClickListener() {
                    @Override
                    public void onEmptyViewClicked(Calendar startDateTime) {
                        Intent intent = new Intent(getContext(), WeekCreateEvent.class);
                        Toast.makeText(getActivity(), "Empty event selected: " + getEventTitle(startDateTime), Toast.LENGTH_SHORT).show();

                        // Set the new event with duration one hour.
                        Calendar endDateTime = (Calendar) startDateTime.clone();
                        endDateTime.add(Calendar.HOUR, 1);

                        Bundle data = new Bundle();
                        data.putString("startDateTime", startDateTime.getTime().toString());
                        data.putString("endDateTime", endDateTime.getTime().toString());
                        intent.putExtras(data);

                        startActivity(intent);
                    }
                });

                // When click on the existing event(Will be the editing event & deleting event)
                weekView.setOnEventClickListener(new WeekView.EventClickListener() {
                    @Override
                    public void onEventClick(final WeekViewEvent event, RectF eventRect) {
                        // Get the layout and render from the Calendar Modal
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        final View mView = getLayoutInflater().inflate(R.layout.calendar_modal, null);

                        CheckBox check = (CheckBox) mView.findViewById(R.id.calendar_Modal_eventCheckBox);
                        TextView title = (TextView) mView.findViewById(R.id.calendar_Modal_eventTitle);
                        TextView startDateTime = (TextView) mView.findViewById(R.id.calendar_Modal_eventStartDateTime);
                        TextView endDateTime = (TextView) mView.findViewById(R.id.calendar_Modal_eventEndDateTime);
                        ImageView editing = (ImageView) mView.findViewById(R.id.calendar_Modal_editing);
                        ImageView deleting = (ImageView) mView.findViewById(R.id.calendar_Modal_deleting);
                        ImageView close = (ImageView) mView.findViewById(R.id.calendar_Modal_eventClose);

                        // Convert to our respective  datetime format of start and end DateTime
                        Timestamp startTimeStamp = new Timestamp(event.getStartTime().getTimeInMillis());
                        Timestamp endTimeStamp = new Timestamp(event.getEndTime().getTimeInMillis());
                        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, YYYY HH:mm", Locale.ENGLISH);
                        String start = sdf.format(startTimeStamp);
                        String end = sdf.format(endTimeStamp);

                        // Put the existing data in the Modal
                        title.setText(event.getName());
                        startDateTime.setText("\n" + start + " - ");
                        endDateTime.setText("\n" + end);

                        // Render the Modal, must be in the last of the code
                        builder.setView(mView);
                        AlertDialog dialog = builder.create();

                        try {
                            dialog.show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // Setting the valid mongoId for the reference with the database
                        int id = (int) event.getId();

                        new AsyncJob.AsyncJobBuilder<Boolean>().doInBackground(new AsyncJob.AsyncAction<Boolean>() {
                            @Override
                            public Boolean doAsync() {

                                if (database.getAllCalbit().get(id).get_id() != null) {
                                    mongoId = database.getAllCalbit().get(id).get_id().toString();
                                }

                                if(database.getAllCalbit().get(id).getReminders() != null) {
                                    mongoReminder = database.getAllCalbit().get(id).getReminders().toString();
                                } else {
                                    mongoReminder = "";
                                }

                                // To allow to run Toast in the async method...
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        check.setEnabled(false);
                                        Toast.makeText(getContext(), "Please wait...", Toast.LENGTH_LONG).show();
                                    }
                                });

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
                                if(database.getAllCalbit().get(id).getCompleted() != null) {
                                    if(database.getAllCalbit().get(id).getCompleted().getStatus()) {
                                        check.setChecked(database.getAllCalbit().get(id).getCompleted().getStatus());
                                        event.setColor(Color.rgb(200, 200, 200));
                                    } else {
                                        check.setChecked(database.getAllCalbit().get(id).getCompleted().getStatus());
                                        event.setColor(Color.rgb(100, 200, 220));
                                    }
                                }

                                Toast.makeText(getContext(), "Done!", Toast.LENGTH_SHORT).show();
                                check.setEnabled(true);

                                check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                        if(isChecked) {
                                            database.updateEventStatusInCalbit(mongoId, isChecked);
                                        }
                                    }
                                });

                                editing.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(getContext(), WeekEditEvent.class);

                                        Bundle data = new Bundle();

                                        data.putString("id", mongoId);
                                        data.putString("title", event.getName());
                                        data.putString("startDateTime", event.getStartTime().getTime().toString());
                                        data.putString("endDateTime", event.getEndTime().getTime().toString());
                                        data.putString("reminderDateTime", mongoReminder);
                                        intent.putExtras(data);

                                        startActivity(intent);
                                        dialog.dismiss();
                                    }
                                });

                                deleting.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        // Delete from Calbitica with the existing data
                                        database.deleteEventInCalbit(mongoId);

                                        // Delete event with existing data(Only 1 data will be found and delete)
                                        for(int i = 0; i < mNewEvents.size(); i++) {
                                            if(mNewEvents.get(i).getId() == event.getId()) {
                                                mNewEvents.remove(event);
                                            }
                                        }

                                        // Refresh the week calendar view.
                                        weekView.notifyDatasetChanged();
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
                        }).create().start();
                    }
                });

                // When click & hold on the existing event
                weekView.setEventLongPressListener(new WeekView.EventLongPressListener() {
                    @Override
                    public void onEventLongPress(final WeekViewEvent event, RectF eventRect) {
                        // For now, not needed...
                    }
                });

                // When scrolling horizontally under the Week Calendar date
                weekView.setScrollListener(new WeekView.ScrollListener() {
                    @Override
                    public void onFirstVisibleDayChanged(Calendar newFirstVisibleDay, Calendar oldFirstVisibleDay) {
                        // When it scroll then...
                        if(oldFirstVisibleDay != null) {
                            // Setting the Calendar title as the scrolled Calendar
                            String currentMonth = DateFormat.getDateInstance(DateFormat.LONG).format(newFirstVisibleDay.getTime());
                            NavigationBar.title.setText(currentMonth.replaceAll("[^a-zA-Z]", "").substring(0, 3) + " "  + newFirstVisibleDay.get(Calendar.YEAR));
                        }
                    }
                });

                // Able to retrieve the data from the Navigation Bar of the drop-down, and change the weekView when clicked
                if (getArguments() != null) {
                    String selectedDate = getArguments().getString("selectedDate");

                    try{
                        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);

                        // Assign to the same calendar to have a link relationship of the Navigation Bar and Today
                        NavigationBar.calendar.setTime(sdf.parse(selectedDate));
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                    }

                    weekView.goToDate(NavigationBar.calendar);
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Allow user to press the button again
                        NavigationBar.nav_today.setEnabled(true);
                        weekMonthCheck = true;
                    }
                }, 3000);
            }
        }).create().start();
    }

    // To get the respective selected date and time
    public static String getEventTitle(Calendar time) {
        // Modify the start Minute to fixed value, rather than minute goes by actual click like 37, 32, etc...
        time.set(Calendar.MINUTE, 0);
        return String.format("Event of %02d:%02d %s/%d", time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), time.get(Calendar.MONTH) + 1, time.get(Calendar.DAY_OF_MONTH));
    }

    // Provide calendar information
    public static ArrayList<WeekViewEvent> getCalendarMonths(int year, int month) {
        // Get the starting point and ending point of the given month. We need this to find the events of the given month.
        Calendar startOfMonth = Calendar.getInstance();
        startOfMonth.set(Calendar.YEAR, year);
        startOfMonth.set(Calendar.MONTH, month - 1);
        startOfMonth.set(Calendar.DAY_OF_MONTH, 1);
        startOfMonth.set(Calendar.HOUR_OF_DAY, 0);
        startOfMonth.set(Calendar.MINUTE, 0);
        startOfMonth.set(Calendar.SECOND, 0);
        startOfMonth.set(Calendar.MILLISECOND, 0);

        Calendar endOfMonth = (Calendar) startOfMonth.clone();
        endOfMonth.set(Calendar.DAY_OF_MONTH, endOfMonth.getMaximum(Calendar.DAY_OF_MONTH));
        endOfMonth.set(Calendar.HOUR_OF_DAY, 23);
        endOfMonth.set(Calendar.MINUTE, 59);
        endOfMonth.set(Calendar.SECOND, 59);

        // Find the events that were added by tapping on empty view and that occurs in the given time frame.
        ArrayList<WeekViewEvent> events = new ArrayList<WeekViewEvent>();
        for (WeekViewEvent event : mNewEvents) {
            if (event.getEndTime().getTimeInMillis() > startOfMonth.getTimeInMillis() &&
                    event.getStartTime().getTimeInMillis() < endOfMonth.getTimeInMillis()) {
                events.add(event);
            }
        }
        return events;
    }

    // To modify the column and the row text to your needs
    public static void setupDateTimeInterpreter(final boolean shortDate) {
        weekView.setDateTimeInterpreter(new DateTimeInterpreter() {
            @Override
            public String interpretDate(Calendar date) {
                // Display Date & Time construct on the top and the left bar
                SimpleDateFormat weekdayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
                String weekday = weekdayNameFormat.format(date.getTime());
                SimpleDateFormat format = new SimpleDateFormat(" M/d", Locale.getDefault());

                // All android api level do not have a standard way of getting the first letter of
                // the week day name. Hence we get the first char programmatically.
                // Details: http://stackoverflow.com/questions/16959502/get-one-letter-abbreviation-of-week-day-of-a-date-in-java#answer-16959657
                if (shortDate)
                    weekday = String.valueOf(weekday.charAt(0));
                return weekday.toUpperCase() + format.format(date.getTime());
            }

            @Override
            public String interpretTime(int hour) {
                if ((hour - 12) == 0) {
                    return "12 PM";
                } else {
                    return hour > 11 ? (hour - 12) + " PM" : (hour == 0 ? "12 AM" : hour + " AM");
                }
            }
        });
    }
}