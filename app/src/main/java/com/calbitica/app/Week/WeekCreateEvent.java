package com.calbitica.app.Week;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.calbitica.app.Models.Calendars.CalbiticaCalendar;
import com.calbitica.app.NavigationBar.NavigationBar;
import com.calbitica.app.R;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.alamkanak.weekview.WeekViewEvent;
import com.calbitica.app.Agenda.AgendaFragment;
import com.calbitica.app.Util.CAWrapper;
import com.calbitica.app.Util.CalbiticaAPI;
import com.calbitica.app.Util.DateUtil;
import com.calbitica.app.Util.UserData;
import com.github.tibolte.agendacalendarview.models.BaseCalendarEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeekCreateEvent extends AppCompatActivity implements CalListResultInterface {
    EditText title = null; // Input CalbiticaCalendar Title
    TextView startDate, startTime, endDate, endTime; // This is just the display from the layout
    Spinner calendarSpinner; // For selecting the calendar
    Switch allDaySwitch;
    JSONObject colorInfo = new JSONObject(); // To make it more information and more easier
    Calendar startDateTime, endDateTime, reminderDateTime; // This is the one that goes CAWrapper
    WeekViewEvent event = null; // The events that will in Week CalbiticaCalendar

    // Helps us format our dates later
    SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);

    // Helps with dynamic spinners
    ArrayAdapter<String> calendarSpinnerAdapter;
    List<String> calbiticaCalendarSummaries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week__create_event);

        title = findViewById(R.id.title);
        calendarSpinner = findViewById(R.id.selectCalendar);
        allDaySwitch = findViewById(R.id.allDaySwitch);

        calbiticaCalendarSummaries.add("");

        // Default the text will be Calbitica Android, by setting as empty for custom
        // TextView to be shown instead
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        // When click the cross image, will go back to WeekFragment
        ImageView close = (ImageView) findViewById(R.id.nav_Close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Get info from WeekFragment
        Bundle bundle = getIntent().getExtras();
        String startDT = bundle.getString("startDateTime");
        String endDT = bundle.getString("endDateTime");
        startDateTime = Calendar.getInstance();
        endDateTime = Calendar.getInstance();
        reminderDateTime = null;

        // From the plus icon from NavigationBar
        try {
            Date startDTDate = (startDT.equals("")) ? new Date() : sdf.parse(startDT);
            Date endDTDate = (endDT.equals("")) ? new Date() : sdf.parse(endDT);

            startDateTime.setTime(startDTDate);
            endDateTime.setTime(endDTDate);

            // high key think this is gonna fail cos its async
            // should delegate to a ui thread
            CAWrapper.getAllCalendars(this, WeekCreateEvent.this);
            calendarSpinnerAdapter = new ArrayAdapter<String>(WeekCreateEvent.this, R.layout.spinner_item,
                    calbiticaCalendarSummaries);

            calendarSpinnerAdapter.setDropDownViewResource(R.layout.spinner_item);
            calendarSpinner.setAdapter(calendarSpinnerAdapter);

        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        // Prompt the Start Date Picker to choose
        startDate = (TextView) findViewById(R.id.startDate);
        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(WeekCreateEvent.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                startDateTime.set(year, month, day);
                                startDate.setText(day + "/" + (month + 1) + "/" + year);
                            }
                        }, year, month, dayOfMonth);
                datePickerDialog.show();
            }
        });

        // Prompt the Start Time Picker to choose
        startTime = (TextView) findViewById(R.id.startTime);
        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(WeekCreateEvent.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                                if (minute < 10) {
                                    startTime.setText(hourOfDay + ":" + "0" + minute);
                                } else {
                                    startTime.setText(hourOfDay + ":" + minute);
                                }

                                startDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                startDateTime.set(Calendar.MINUTE, minute);
                            }
                        }, hourOfDay, minute, android.text.format.DateFormat.is24HourFormat(WeekCreateEvent.this));
                timePickerDialog.show();
            }
        });

        // Default startDateTime will be automatically configure
        if (!startDT.equals("")) {
            int startMonth = startDateTime.get(Calendar.MONTH) + 1;
            startDate.setText(startDateTime.get(Calendar.DAY_OF_MONTH) + "/" + startMonth + "/"
                    + startDateTime.get(Calendar.YEAR));

            if (startDateTime.get(Calendar.MINUTE) < 10) {
                startTime.setText(
                        startDateTime.get(Calendar.HOUR_OF_DAY) + ":" + "0" + startDateTime.get(Calendar.MINUTE));
            } else {
                startTime.setText(startDateTime.get(Calendar.HOUR_OF_DAY) + ":" + startDateTime.get(Calendar.MINUTE));
            }
        }

        // Prompt the End Date Picker to choose
        endDate = (TextView) findViewById(R.id.endDate);
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(WeekCreateEvent.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                endDateTime.set(year, month, day);
                                endDate.setText(day + "/" + (month + 1) + "/" + year);
                            }
                        }, year, month, dayOfMonth);
                datePickerDialog.show();
            }
        });

        // Prompt the End Time Picker to choose
        endTime = (TextView) findViewById(R.id.endTime);
        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(WeekCreateEvent.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                                if (minute < 10) {
                                    endTime.setText(hourOfDay + ":" + "0" + minute);
                                } else {
                                    endTime.setText(hourOfDay + ":" + minute);
                                }

                                endDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                endDateTime.set(Calendar.MINUTE, minute);
                            }
                        }, hourOfDay, minute, android.text.format.DateFormat.is24HourFormat(WeekCreateEvent.this));
                timePickerDialog.show();
            }
        });

        // Default endDateTime will be automatically configure
        if (!endDT.equals("")) {
            int endMonth = endDateTime.get(Calendar.MONTH) + 1;
            endDate.setText(
                    endDateTime.get(Calendar.DAY_OF_MONTH) + "/" + endMonth + "/" + endDateTime.get(Calendar.YEAR));

            if (endDateTime.get(Calendar.MINUTE) < 10) {
                endTime.setText(endDateTime.get(Calendar.HOUR_OF_DAY) + ":" + "0" + endDateTime.get(Calendar.MINUTE));
            } else {
                endTime.setText(endDateTime.get(Calendar.HOUR_OF_DAY) + ":" + endDateTime.get(Calendar.MINUTE));
            }
        }

        // Prompt the Reminder Date Picker to choose
        final TextView reminderDate = (TextView) findViewById(R.id.reminderDate);
        reminderDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(WeekCreateEvent.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                if (reminderDate.getText() != null) {
                                    reminderDateTime = Calendar.getInstance();

                                    reminderDateTime.set(year, month, day);
                                    reminderDate.setText(day + "/" + (month + 1) + "/" + year);
                                }
                            }
                        }, year, month, dayOfMonth);
                datePickerDialog.show();
            }
        });

        // Prompt the Reminder Time Picker to choose
        final TextView reminderTime = (TextView) findViewById(R.id.reminderTime);
        reminderTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(WeekCreateEvent.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                                if (reminderTime.getText() != null) {
                                    reminderDateTime = Calendar.getInstance();

                                    if (minute < 10) {
                                        reminderTime.setText(hourOfDay + ":" + "0" + minute);
                                    } else {
                                        reminderTime.setText(hourOfDay + ":" + minute);
                                    }

                                    reminderDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    reminderDateTime.set(Calendar.MINUTE, minute);
                                }
                            }
                        }, hourOfDay, minute, android.text.format.DateFormat.is24HourFormat(WeekCreateEvent.this));
                timePickerDialog.show();
            }
        });

        // Default reminderDateTime will be automatically configure
        if (reminderDateTime != null) {
            int reminderMonth = reminderDateTime.get(Calendar.MONTH) + 1;
            reminderDate.setText(reminderDateTime.get(Calendar.DAY_OF_MONTH) + "/" + reminderMonth + "/"
                    + reminderDateTime.get(Calendar.YEAR));

            if (reminderDateTime.get(Calendar.MINUTE) < 10) {
                reminderTime.setText(
                        reminderDateTime.get(Calendar.HOUR_OF_DAY) + ":" + "0" + reminderDateTime.get(Calendar.MINUTE));
            } else {
                reminderTime.setText(
                        reminderDateTime.get(Calendar.HOUR_OF_DAY) + ":" + reminderDateTime.get(Calendar.MINUTE));
            }
        }
    }

    // Right Menu Bar Item: Create the 'container'/space
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.event_menu, menu);
        return true;
    }

    // When the Right Menu Bar Item (Tick Image) is Selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.ok) {
            // Due to Schedule CalbiticaCalendar "No events" is a empty view as default
            if(title.getText().toString().equals("") || title.getText().toString().equals("No events") ||
               startDate.getText().toString().equals("") || startTime.getText().toString().equals("") ||
               endDate.getText().toString().equals("") || endTime.getText().toString().equals("")) {
                Toast.makeText(WeekCreateEvent.this,"Please fill in all the fields", Toast.LENGTH_SHORT).show();
            } else if (startDateTime.getTime().getTime() >= endDateTime.getTime().getTime()) {
                // Making use of the Epoch & Unix Timestamp Conversion Tools, can easily tell all the information of the dates
                Toast.makeText(WeekCreateEvent.this,"Start date and time must be before end date and time", Toast.LENGTH_SHORT).show();
            } else {
                // Setting the valid mongoId for the reference with the database
                int mongoId = (event != null) ? (int) event.getId() : WeekFragment.mNewEvents.size();

                if(NavigationBar.selectedPages == "nav_week") {
                    // Create a new event for Week CalbiticaCalendar
                    event = new WeekViewEvent(mongoId, title.getText().toString(), startDateTime, endDateTime);
                    try {
//                        int colorText = (Integer) colorInfo.get("color");
//                        event.setColor(colorText);
                        WeekFragment.mNewEvents.add(event);

                        // Refresh the week view. onMonthChange will be called again.
                        WeekFragment.weekView.notifyDatasetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (NavigationBar.selectedPages == "nav_schedule") {
                    // Create a new event for Schedule CalbiticaCalendar
                    try {
                        int colorText = (Integer) colorInfo.get("color");

                        BaseCalendarEvent allEvent = new BaseCalendarEvent(title.getText().toString(), "", "", colorText, startDateTime, endDateTime, false);
                        allEvent.setId(mongoId);
                        AgendaFragment.eventList.add(allEvent);

                        // Schedule CalbiticaCalendar will also re-render the events as well
                        AgendaFragment.agendaView.init(AgendaFragment.eventList, AgendaFragment.minDate, AgendaFragment.maxDate, Locale.getDefault(), AgendaFragment.calendarPickerController);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                // Make Post Request to Calbitica API
                HashMap<String, String> calbit = new HashMap<>();
                String startDateStr = DateUtil.localToUTC(startDateTime.getTime());
                String endDateStr = DateUtil.localToUTC(endDateTime.getTime());
                calbit.put("start", startDateStr);
                calbit.put("end", endDateStr);
                calbit.put("allDay", "" + allDaySwitch.isChecked()); // TODO: don't hardcode this please
                calbit.put("calendarID", calendarSpinner.getSelectedItem().toString()); // TODO: don't hardcode this please
                calbit.put("display", "true");
                calbit.put("isDump", "false");
                calbit.put("title", title.getText().toString());

                // TODO: Save reminders
                // TODO: Add location

                createCalbit(calbit);
            }
        }

    return super.onOptionsItemSelected(item);

    }

    // dynamic spinner
    public void onCalendarListResult(List<CalbiticaCalendar> calbiticaCalendars) {
        calbiticaCalendarSummaries.clear();

        for(CalbiticaCalendar c : calbiticaCalendars)
            calbiticaCalendarSummaries.add(c.getSummary());

        calendarSpinnerAdapter.notifyDataSetChanged();
    }

    // API calls
    public void createCalbit(HashMap<String, String> calbit) {
        // Retrieve the JWT
        String oldJWT = UserData.get("jwt", WeekCreateEvent.this);

        // Build the API Call
        Call<HashMap<String, Object>> apiCall = CalbiticaAPI.getInstance(oldJWT).calbit().createCalbit(calbit);

        // Make the API Call
        apiCall.enqueue(new Callback<HashMap<String, Object>>() {
            @Override
            public void onResponse(Call<HashMap<String, Object>> call, Response<HashMap<String, Object>> response) {
                if (!response.isSuccessful()) {
                    Log.d("SLEEP CALL", response.toString());
                    return;
                }
                try {

                    HashMap<String, Object> responseData = response.body();
                    // Handle new JWT returned, if any
                    if (responseData.containsKey("jwt")) {
                        // Handle JWT
                        HashMap<String, String> user = new HashMap<>();
                        user.put("jwt", responseData.get("jwt").toString());

                        UserData.save(user, WeekCreateEvent.this);
                    }

                    // Close this activity!

                    System.out.println("EVENT CREATED " + response.body().toString());
                    Toast.makeText(WeekCreateEvent.this, "Event created", Toast.LENGTH_SHORT).show();
                    finish();
                } catch (Exception e) {
                    Log.d("API JWT FAILED", e.getLocalizedMessage());
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, Object>> call, Throwable t) {
                Log.d("Create event FAILED", call.toString());
                Log.d("Create event MORE DETAILS", t.getLocalizedMessage());
            }
        });
    }
}
