package edu.nyp.calbiticaandroid;

import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;;
import java.util.List;
import java.util.Locale;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class WeekFragment extends Fragment {
    public static WeekView weekView;
    public static ArrayList<WeekViewEvent> mNewEvents;

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
        View view = inflater.inflate(R.layout.fragment_week, container, false);//Inflate Layout
        return view;//return default view
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        weekView = getActivity().findViewById(R.id.weekView);
        mNewEvents = new ArrayList<WeekViewEvent>();

        // Get the information from the getCalendarMonths(required)
        weekView.setMonthChangeListener(new MonthLoader.MonthChangeListener() {
            @Override
            public List<WeekViewEvent> onMonthChange(int newYear, int newMonth) {
                // Populate the week view with the events that was added by tapping on empty view.
                List<WeekViewEvent> events = new ArrayList<WeekViewEvent>();
                ArrayList<WeekViewEvent> newEvents = getCalendarMonths(newYear, newMonth);
                events.addAll(newEvents);
                return events;
            }
        });

        // Get the event from firebase
        edu.nyp.calbiticaandroid.Database.Firebase firebase = new edu.nyp.calbiticaandroid.Database.Firebase();
        firebase.getEventsFromFirebase();

        // When click on the empty event(Will be the creating event)
        weekView.setEmptyViewClickListener(new WeekView.EmptyViewClickListener() {
            @Override
            public void onEmptyViewClicked(Calendar startDateTime) {
                Intent intent = new Intent(getContext(), Week_CreateEvent.class);
                Toast.makeText(getActivity(), "Empty view click" + getEventTitle(startDateTime), Toast.LENGTH_SHORT).show();

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

        // When click on the existing event(Will be the editing event and deleting event option)
        weekView.setOnEventClickListener(new WeekView.EventClickListener() {
            @Override
            public void onEventClick(WeekViewEvent event, RectF eventRect) {
                System.out.println("event " + event.getId());
            }
        });

        // Set up a date time interpreter to interpret how the date and time will be formatted in
        // the week view. This is optional.
        setupDateTimeInterpreter(true);

        // Able to retrieve the data from the Navigation Bar of the drop-down
        if (getArguments() != null) {
            String selectedDate = getArguments().getString("selectedDate");
            Calendar chooseDate = Calendar.getInstance();

            try{
                SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                chooseDate.setTime(sdf.parse(selectedDate));
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }

            weekView.goToDate(chooseDate);
        }
    }

    // To get the respective selected date and time
    protected String getEventTitle(Calendar time) {
        // Modify the start Minute to fixed value, rather than minute goes by actual click like 37, 32, etc...
        time.set(Calendar.MINUTE, 0);
        return String.format("Event of %02d:%02d %s/%d", time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), time.get(Calendar.MONTH) + 1, time.get(Calendar.DAY_OF_MONTH));
    }

    // Provide calendar information
    private ArrayList<WeekViewEvent> getCalendarMonths(int year, int month) {
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
    private void setupDateTimeInterpreter(final boolean shortDate) {
        weekView.setDateTimeInterpreter(new DateTimeInterpreter() {
            @Override
            public String interpretDate(Calendar date) {
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