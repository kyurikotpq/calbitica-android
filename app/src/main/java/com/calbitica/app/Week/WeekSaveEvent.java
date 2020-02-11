package com.calbitica.app.Week;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.calbitica.app.Models.Calbit.Calbit;
import com.calbitica.app.Models.Calendars.CalbiticaCalendar;
import com.calbitica.app.NavigationBar.NavigationBar;
import com.calbitica.app.R;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import com.calbitica.app.Util.CalListResultInterface;
import com.calbitica.app.Util.CalbiticaAPI;
import com.calbitica.app.Util.DateUtil;
import com.calbitica.app.Util.UserData;
import com.github.tibolte.agendacalendarview.models.BaseCalendarEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeekSaveEvent extends AppCompatActivity implements CalListResultInterface {
    TextView navTitleTV = null;
    EditText title = null; // Input CalbiticaCalendar Title
    TextView startDate, startTime, endDate, endTime; // This is just the display from the layout
    TextView reminderDate, reminderTime;
    Spinner calendarSpinner; // For selecting the calendar
    Switch allDaySwitch;
    Calendar startDateTime, endDateTime, reminderDateTime; // This is the one that goes CAWrapper

    // Helps with dynamic spinners
    ArrayAdapter<String> calendarSpinnerAdapter;
    List<CalbiticaCalendar> calbiticaCalendarsList;
    String calendarID = "";
    List<String> calbiticaCalendarSummaries = new ArrayList<>();

    // Keeps track of Event's Mongo ID and google ID
    String _id = "";
    String googleID = "";
    long wveIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week__create_event);

        // Assign view to variables
        title = findViewById(R.id.title);
        calendarSpinner = findViewById(R.id.selectCalendar);
        allDaySwitch = findViewById(R.id.allDaySwitch);
        startDate = findViewById(R.id.startDate);
        startTime = (TextView) findViewById(R.id.startTime);
        endDate = findViewById(R.id.endDate);
        endTime = (TextView) findViewById(R.id.endTime);
        reminderDate = (TextView) findViewById(R.id.reminderDate);
        reminderTime = (TextView) findViewById(R.id.reminderTime);

        calbiticaCalendarSummaries.add("");

        // Setup other global vars
        startDateTime = Calendar.getInstance();
        endDateTime = Calendar.getInstance();
        reminderDateTime = Calendar.getInstance();

        // Default the text will be Calbitica Android, by setting as empty for custom
        // TextView to be shown instead
        navTitleTV = findViewById(R.id.nav_Title);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        // When click the cross image, will go back to WeekFragment
        ImageView close = (ImageView) findViewById(R.id.nav_Close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

        setupCalendarSpinner();

        // Get info from WeekFragment
        Bundle bundle = getIntent().getExtras();

        // Check if this is a create or update
        _id = bundle.getString("id");
        wveIndex = bundle.getLong("wveIndex");

        if (_id != null && !_id.equals("")) {
            // EDITING: populate the views accordingly
            title.setText(bundle.getString("title"));
            allDaySwitch.setChecked(bundle.getBoolean("legitAllDay"));

            String reminderStr = bundle.getString("reminderDateTime");
            if (reminderStr != null && !reminderStr.equals("")) {
                Date reminderDateObj = DateUtil.utcStringToLocalDate(reminderStr);
                reminderDateTime.setTime(reminderDateObj);

                reminderDate.setText(DateUtil.ddMMMyyyy(reminderDateTime.getTime()));
                reminderTime.setText(DateUtil.HHmm(reminderDateTime.getTime()));
            }

            String sentGoogleID = bundle.getString("googleID");
            if (sentGoogleID != null && !sentGoogleID.equals("")) {
                googleID = sentGoogleID;
            }

            calendarID = bundle.getString("calendarID");
            navTitleTV.setText("Edit Event");
        } else {
            navTitleTV.setText("Create Event");
            reminderDate.setText(null);
            reminderTime.setText(null);
        }

        String startDT = bundle.getString("startDateTime");
        String endDT = bundle.getString("endDateTime");

        // From the plus icon from NavigationBar
        try {
            Date startDTDate = (startDT.equals(""))
                    ? new Date()
                    : DateUtil.utcStringToLocalDate(startDT);
            Date endDTDate = (endDT.equals(""))
                    ? new Date()
                    : DateUtil.utcStringToLocalDate(endDT);

            startDateTime.setTime(startDTDate);
            endDateTime.setTime(endDTDate);

            // Default startDateTime will be automatically configure
            startDate.setText(DateUtil.ddMMMyyyy(startDTDate));
            startTime.setText(DateUtil.HHmm(startDTDate));
            endDate.setText(DateUtil.ddMMMyyyy(endDTDate));
            endTime.setText(DateUtil.HHmm(endDTDate));

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Prompt the Start Date Picker to choose
        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(WeekSaveEvent.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                startDateTime.set(year, month, day);
                                Date newStartDate = startDateTime.getTime();
                                startDate.setText(DateUtil.ddMMMyyyy(newStartDate));
                            }
                        }, year, month, dayOfMonth);
                datePickerDialog.show();
            }
        });

        // Prompt the Start Time Picker to choose
        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(WeekSaveEvent.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                                String startTimeStr = (minute < 10)
                                        ? hourOfDay + ":" + "0" + minute
                                        : hourOfDay + ":" + minute;

                                startTime.setText(startTimeStr);

                                startDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                startDateTime.set(Calendar.MINUTE, minute);
                            }
                        }, hourOfDay, minute, android.text.format.DateFormat.is24HourFormat(WeekSaveEvent.this));
                timePickerDialog.show();
            }
        });


        // Prompt the End Date Picker to choose
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(WeekSaveEvent.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                endDateTime.set(year, month, day);
                                Date newEndDate = endDateTime.getTime();
                                endDate.setText(DateUtil.ddMMMyyyy(newEndDate));
                            }
                        }, year, month, dayOfMonth);
                datePickerDialog.show();
            }
        });

        // Prompt the End Time Picker to choose
        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(WeekSaveEvent.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                                String endTimeStr = (minute < 10)
                                        ? hourOfDay + ":" + "0" + minute
                                        : hourOfDay + ":" + minute;

                                endTime.setText(endTimeStr);

                                endDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                endDateTime.set(Calendar.MINUTE, minute);
                            }
                        }, hourOfDay, minute, android.text.format.DateFormat.is24HourFormat(WeekSaveEvent.this));
                timePickerDialog.show();
            }
        });


        // Prompt the Reminder Date Picker to choose
        reminderDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(WeekSaveEvent.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                if (reminderDate.getText() != null) {
                                    reminderDateTime.set(year, month, day);

                                    Date newReminderDateTime = reminderDateTime.getTime();
                                    reminderDate.setText(DateUtil.ddMMMyyyy(newReminderDateTime));
                                }
                            }
                        }, year, month, dayOfMonth);
                datePickerDialog.show();
            }
        });

        // Prompt the Reminder Time Picker to choose
        reminderTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(WeekSaveEvent.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                                if (reminderTime.getText() != null) {
                                    reminderDateTime = Calendar.getInstance();

                                    String reminderTimeStr = (minute < 10)
                                            ? hourOfDay + ":" + "0" + minute
                                            : hourOfDay + ":" + minute;

                                    reminderTime.setText(reminderTimeStr);

                                    reminderDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    reminderDateTime.set(Calendar.MINUTE, minute);
                                }
                            }
                        }, hourOfDay, minute, android.text.format.DateFormat.is24HourFormat(WeekSaveEvent.this));
                timePickerDialog.show();
            }
        });

    }

    public void setupCalendarSpinner() {
        // should delegate to a ui thread
        CAWrapper.getAllCalendars(getApplicationContext(), WeekSaveEvent.this);
        calendarSpinnerAdapter = new ArrayAdapter<String>(WeekSaveEvent.this, R.layout.spinner_item,
                calbiticaCalendarSummaries);

        calendarSpinnerAdapter.setDropDownViewResource(R.layout.spinner_item);
        calendarSpinner.setAdapter(calendarSpinnerAdapter);
    }

    // dynamic spinner
    public void onCalendarListResult(List<CalbiticaCalendar> calbiticaCalendars) {
        calbiticaCalendarsList = calbiticaCalendars;
        calbiticaCalendarSummaries.clear();

        int index = 0;
        for (int i = 0; i < calbiticaCalendars.size(); i++) {
            CalbiticaCalendar c = calbiticaCalendars.get(i);

            if (c.getGoogleID().equals(calendarID))
                index = i;

            calbiticaCalendarSummaries.add(c.getSummary());
        }

        calendarSpinnerAdapter.notifyDataSetChanged();
        calendarSpinner.setSelection(index);
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
            if (title.getText().toString().equals("") || title.getText().toString().equals("No events") ||
                    startDate.getText().toString().equals("") || startTime.getText().toString().equals("") ||
                    endDate.getText().toString().equals("") || endTime.getText().toString().equals("")) {
                Toast.makeText(WeekSaveEvent.this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
            } else if (startDateTime.getTime().getTime() >= endDateTime.getTime().getTime()) {
                // Making use of the Epoch & Unix Timestamp Conversion Tools, can easily tell all the information of the dates
                Toast.makeText(WeekSaveEvent.this, "Start date and time must be before end date and time", Toast.LENGTH_SHORT).show();
            } else {
                // Setting the valid mongoId for the reference with the database
                boolean isEditing = (_id != null && !_id.equals("") && wveIndex != -1);

                // Prevent spam
                item.setEnabled(false);

                Toast.makeText(WeekSaveEvent.this, "Saving your event...", Toast.LENGTH_SHORT).show();

                if (NavigationBar.selectedPages == "nav_week") {
                    int newWVEIndex = isEditing
                            ? (int) wveIndex
                            : WeekFragment.mNewEvents.size();

                    try {
                        // Create a new event for Week CalbiticaCalendar
                        WeekViewEvent newWVE = new WeekViewEvent(newWVEIndex, title.getText().toString(), startDateTime, endDateTime);

                        if (isEditing) {
                            Calbit currentCalbit = WeekFragment.listOfCalbits.get((int) wveIndex);

                            int newColor = currentCalbit.getCompleted() != null
                                    && currentCalbit.getCompleted().getStatus()
                                    ? R.color.gray_3
                                    : R.color.blue_3;
                            newWVE.setColor(getResources().getColor(newColor, null));
                            WeekFragment.mNewEvents.set(newWVEIndex, newWVE);

                        } else {
                            int newColor = R.color.blue_3;
                            newWVE.setColor(getResources().getColor(newColor, null));
                            WeekFragment.mNewEvents.add(newWVE);
                        }

                        // Refresh the week view. onMonthChange will be called again.
//                        WeekFragment.weekView.notifyDatasetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (NavigationBar.selectedPages == "nav_schedule") {
                    // Create a new event for Schedule CalbiticaCalendar
                    int newWVEIndex = isEditing
                            ? (int) wveIndex
                            : AgendaFragment.eventList.size();

                    try {
                        int colorText = R.color.blue_3;

                        BaseCalendarEvent allEvent = new BaseCalendarEvent(title.getText().toString(), "", "", colorText, startDateTime, endDateTime, false);
                        allEvent.setId(newWVEIndex);
                        AgendaFragment.eventList.add(allEvent);

                        // Schedule CalbiticaCalendar will need to re-render the events as well
//                        AgendaFragment.agendaView.init(AgendaFragment.eventList, AgendaFragment.minDate, AgendaFragment.maxDate, Locale.getDefault(), AgendaFragment.calendarPickerController);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // Make Post Request to Calbitica API
                String titleStr = title.getText().toString();
                Boolean isAllDay = allDaySwitch.isChecked();
                String calendarSummary = calendarSpinner.getSelectedItem().toString();
                String calendarID = "";
                if (calbiticaCalendarsList != null) {
                    for (CalbiticaCalendar c : calbiticaCalendarsList) {
                        if (c.getSummary().equals(calendarSummary)) {
                            calendarID = c.getGoogleID();
                            break;
                        }
                    }
                }

                HashMap<String, String> calbit = new HashMap<>();

                String startDateStr = isAllDay ? DateUtil.localToUTCAllDay(startDateTime.getTime())
                        : DateUtil.localToUTC(startDateTime.getTime());
                String endDateStr = isAllDay ? DateUtil.localToUTCAllDay(endDateTime.getTime())
                        : DateUtil.localToUTC(endDateTime.getTime());

                if (isAllDay && startDateStr.equals(endDateStr)) {
                    endDateTime.add(Calendar.DATE, 1);
                    endDateStr = DateUtil.localToUTCAllDay(endDateTime.getTime());
                }

                if (!googleID.equals("")) {
                    calbit.put("googleID", googleID);
                }

                calbit.put("start", startDateStr);
                calbit.put("end", endDateStr);

                calbit.put("allDay", "" + isAllDay);
                calbit.put("calendarID", calendarID);
                calbit.put("display", "true");
                calbit.put("isDump", "false");
                calbit.put("title", titleStr);

                if (!reminderDate.getText().equals("")) {
                    String reminders = DateUtil.localToUTC(reminderDateTime.getTime());
                    calbit.put("reminders", reminders);
                }

                saveCalbit(calbit);
            }
        }

        return super.onOptionsItemSelected(item);

    }

    // API calls
    public void saveCalbit(HashMap<String, String> calbit) {
        // Retrieve the JWT
        String oldJWT = UserData.get("jwt", getApplicationContext());

        // Build the API Call
        Boolean isEditing = (_id != null && !_id.equals(""));
        Call<HashMap<String, Object>> apiCall = isEditing
                ? CalbiticaAPI.getInstance(oldJWT).calbit().updateCalbit(_id, calbit)
                : CalbiticaAPI.getInstance(oldJWT).calbit().createCalbit(calbit);

        // Make the API Call
        apiCall.enqueue(new Callback<HashMap<String, Object>>() {
            @Override
            public void onResponse(Call<HashMap<String, Object>> call, Response<HashMap<String, Object>> response) {
                if (!response.isSuccessful()) {
                    try {
                        Toast.makeText(WeekSaveEvent.this, "We don't support all-day events for now, sorry!", Toast.LENGTH_SHORT).show();
                        return;
                    } catch (Exception e) {
                    }
                }
                try {
                    HashMap<String, Object> responseData = response.body();
                    // Handle new JWT returned, if any
                    if (responseData.containsKey("jwt")) {
                        // Handle JWT
                        HashMap<String, String> user = new HashMap<>();
                        user.put("jwt", responseData.get("jwt").toString());

                        UserData.save(user, getApplicationContext());
                    }

                    // Close this activity!
                    String successTxt = isEditing ? "Event updated." : "Event created.";
                    Toast.makeText(WeekSaveEvent.this, successTxt, Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                } catch (Exception e) {
                    Log.d("API JWT FAILED", e.getLocalizedMessage());
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, Object>> call, Throwable t) {
                Toast.makeText(WeekSaveEvent.this, "Check your internet connection and try again.",
                        Toast.LENGTH_SHORT).show();
                Log.d("Create event FAILED", call.toString());
                Log.d("Create event MORE DETAILS", t.getLocalizedMessage());
            }
        });
    }
}
