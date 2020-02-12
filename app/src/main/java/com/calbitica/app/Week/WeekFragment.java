package com.calbitica.app.Week;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.arasthel.asyncjob.AsyncJob;
import com.calbitica.app.Util.CAWrapper;
import com.calbitica.app.Models.Calbit.Calbit;
import com.calbitica.app.Models.Calbit.TaskCompleted;
import com.calbitica.app.NavigationBar.NavigationBar;
import com.calbitica.app.R;
import com.calbitica.app.Util.CalbitResultInterface;
import com.calbitica.app.Util.DateUtil;

public class WeekFragment extends Fragment implements CalbitResultInterface {
    public static WeekView weekView;                                            // Mostly used from NavigationBar refresh, etc...(Week Calender)
    public static ArrayList<WeekViewEvent> mNewEvents = new ArrayList<>();      // Mostly used from NavigationBar refresh, etc...(Event in Week CalbiticaCalendar)
    public static boolean weekMonthCheck;                                       // Ensure the weekView is loaded finished
    public static List<Calbit> listOfCalbits;               // Temp storage of calbit list from API
    private String currentSelectedMongoID = null;                                              // Particular events(Mainly for edit and delete)

    // Elements in our dialog
    CheckBox check;
    TextView title, startDateTime, endDateTime;
    ImageView editing, deleting, close;

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
                    CAWrapper.getAllCalbits(getActivity().getApplicationContext(), WeekFragment.this);
                }
                break;
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        // Get the events from CAWrapper
        CAWrapper.getAllCalbits(getActivity().getApplicationContext(), WeekFragment.this);

        new AsyncJob.AsyncJobBuilder<Boolean>().doInBackground(new AsyncJob.AsyncAction<Boolean>() {
            @Override
            public Boolean doAsync() {
                weekMonthCheck = false;
                weekView = getActivity().findViewById(R.id.weekView);
                mNewEvents = new ArrayList<WeekViewEvent>();
                listOfCalbits = new ArrayList<>();

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

                return true;
            }
        }).doWhenFinished(new AsyncJob.AsyncResultAction<Boolean>() {
            @Override
            public void onResult(Boolean result) {
                // When click on the empty cell -> create event
                weekView.setEmptyViewClickListener(new WeekView.EmptyViewClickListener() {
                    @Override
                    public void onEmptyViewClicked(Calendar startDateTime) {
                        Intent intent = new Intent(getContext(), WeekSaveEvent.class);

                        // Set the new event with duration 30mins
                        Calendar endDateTime = (Calendar) startDateTime.clone();
                        endDateTime.add(Calendar.MINUTE, 30);

                        // Pass over the data
                        Bundle data = new Bundle();

                        data.putString("startDateTime", DateUtil.localToUTC(startDateTime.getTime()));
                        data.putString("endDateTime", DateUtil.localToUTC(endDateTime.getTime()));
                        intent.putExtras(data);

                        startActivityForResult(intent, 122); // show the saving activity
                    }
                });

                // When click on the existing event -> show detail dialog
                weekView.setOnEventClickListener(new WeekView.EventClickListener() {
                    @Override
                    public void onEventClick(final WeekViewEvent event, RectF eventRect) {
                        // Get the layout and render from the CalbiticaCalendar Modal
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        final View mView = getLayoutInflater().inflate(R.layout.calendar_modal, null);

                        check = mView.findViewById(R.id.calendar_Modal_eventCheckBox);
                        title = mView.findViewById(R.id.calendar_Modal_eventTitle);
                        startDateTime = mView.findViewById(R.id.calendar_Modal_eventStartDateTime);
                        endDateTime = mView.findViewById(R.id.calendar_Modal_eventEndDateTime);
                        editing = mView.findViewById(R.id.calendar_Modal_editing);
                        deleting = mView.findViewById(R.id.calendar_Modal_deleting);
                        close = mView.findViewById(R.id.calendar_Modal_eventClose);

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

                        // Prepare for an edit/delete activity
                        // get the currentSelectedMongoID
                        int wveIndex = (int) event.getId();
                        Calbit clickedOnCalbit = listOfCalbits.get(wveIndex);

                        currentSelectedMongoID = clickedOnCalbit.get_id().toString();
                        String mongoReminder = (clickedOnCalbit.getReminders() != null
                        && clickedOnCalbit.getReminders().size() > 0)
                                ? DateUtil.localToUTC(clickedOnCalbit.getReminders().get(0))
                                : "";

                        if (clickedOnCalbit.getCompleted() != null) {
                            check.setChecked(clickedOnCalbit.getCompleted().getStatus());
                        } else {
                            check.setChecked(false);
                        }

                        close.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                        check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                // Update the database
                                HashMap<String, Boolean> completion = new HashMap<>();
                                completion.put("status", isChecked);
                                CAWrapper.updateCalbitCompletion(getActivity().getApplicationContext(),
                                        currentSelectedMongoID, completion,
                                        WeekFragment.this);

                                // We will do local updates here, regardless of the result of the API call,
                                // as sooner or later the real changes will show up.
                                int newColor = (isChecked) ? R.color.gray_3 : R.color.blue_3;
                                event.setColor(getResources().getColor(newColor, null));

                                // update on the list of calbits too
                                clickedOnCalbit.setCompleted(new TaskCompleted(isChecked));

                                // Refresh the Week Calendar to make any changes
                                weekView.notifyDatasetChanged();
                            }
                        });

                        editing.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getContext(), WeekSaveEvent.class);

                                Bundle data = new Bundle();
                                data.putString("id", currentSelectedMongoID);
                                data.putLong("wveIndex", event.getId());
                                data.putString("title", event.getName());
                                data.putString("calendarID", clickedOnCalbit.getCalendarID());

                                data.putString("startDateTime", DateUtil.localToUTC(event.getStartTime().getTime()));
                                data.putString("endDateTime", DateUtil.localToUTC(event.getEndTime().getTime()));

                                data.putString("reminderDateTime", mongoReminder);
                                data.putBoolean("legitAllDay", clickedOnCalbit.getAllDay());
                                data.putString("googleID", clickedOnCalbit.getGoogleID());

                                intent.putExtras(data);

                                startActivityForResult(intent, 122);
                                dialog.dismiss();
                            }
                        });

                        deleting.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Delete from Calbitica with the existing data
                                CAWrapper.deleteCalbit(getActivity().getApplicationContext(),
                                        currentSelectedMongoID,
                                        WeekFragment.this);

                                // Delete event with existing data(Only 1 data will be found and delete)
                                // Sadly, this is the only way to do it...
                                for (int i = 0; i < mNewEvents.size(); i++) {
                                    if (mNewEvents.get(i).getId() == event.getId()) {
                                        mNewEvents.remove(event);
                                        listOfCalbits.removeIf(c -> c.get_id().equals(currentSelectedMongoID));
                                    }
                                }

                                // reset the indices
                                for (int i = 0; i < mNewEvents.size(); i++) {
                                    mNewEvents.get(i).setId(i);
                                }

                                // Refresh the week calendar view.
                                weekView.notifyDatasetChanged();

                                Toast.makeText(getActivity(), "Event deleted.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });

                        close.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
//                            }
//                        }).create().start();
                    }
                });

                // When scrolling horizontally under the Week CalbiticaCalendar date
                weekView.setScrollListener(new WeekView.ScrollListener() {
                    @Override
                    public void onFirstVisibleDayChanged(Calendar newFirstVisibleDay, Calendar oldFirstVisibleDay) {
                        // When it scroll then...
                        if (oldFirstVisibleDay != null) {
                            // Setting the CalbiticaCalendar title as the scrolled CalbiticaCalendar
                            String currentMonth = DateFormat.getDateInstance(DateFormat.LONG).format(newFirstVisibleDay.getTime());
                            NavigationBar.title.setText(currentMonth.replaceAll("[^a-zA-Z]", "").substring(0, 3) + " " + newFirstVisibleDay.get(Calendar.YEAR));
                        }
                    }
                });

                // Able to retrieve the data from the Navigation Bar of the drop-down, and change the weekView when clicked
                if (getArguments() != null) {
                    String selectedDate = getArguments().getString("selectedDate");

                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm:ss z yyyy", Locale.ENGLISH);

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

    // Other Week View setup & enhancements
    // To get the respective selected date and time
    public static String getEventTitle(Calendar time) {
        // Modify the start Minute to fixed value, rather than minute goes by actual click like 37, 32, etc...
        time.set(Calendar.MINUTE, 0);
        return String.format("Event of %02d:%02d %s/%d", time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), time.get(Calendar.MONTH) + 1, time.get(Calendar.DAY_OF_MONTH));
    }

    // Provide calendar information for Weekview
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

    // API handlers:
    // Handle result of calbit list
    @Override
    public void onCalbitListResult(List<Calbit> calbitList) {
        listOfCalbits.clear();
        mNewEvents.clear();

        // save each calbit as a new WeekViewEvent
        for (int i = 0; i < calbitList.size(); i++) {
            Calbit currentCalbit = calbitList.get(i);

            // Set start date and time
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

            // Render all the data into WeekView CalbiticaCalendar
            // event.getId() = i later on
            WeekViewEvent newWVE = new WeekViewEvent(i, currentCalbit.getSummary(), startDateTime, endDateTime);

            int newColor = (currentCalbit.getCompleted().getStatus()) ? R.color.gray_3 : R.color.blue_3;
            newWVE.setColor(getActivity().getResources().getColor(newColor, null));

            newWVE.setAllDay(currentCalbit.getAllDay());
            mNewEvents.add(newWVE);
        }

        // Refresh the Week View
        listOfCalbits = calbitList;
        weekView.notifyDatasetChanged();
    }

    // Handle result of failed completion update
    @Override
    public void onCalbitCompletionFailure() {
        Toast.makeText(getContext(), "That action didn't go through; please try again.", Toast.LENGTH_SHORT).show();
    }
}