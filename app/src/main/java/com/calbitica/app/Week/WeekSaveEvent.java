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
import com.calbitica.app.Util.CalbiticaAPI;
import com.calbitica.app.Util.DateUtil;
import com.calbitica.app.Util.UserData;
import com.github.tibolte.agendacalendarview.models.BaseCalendarEvent;

import org.json.JSONException;
import org.json.JSONObject;

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
    Spinner calendarSpinner; // For selecting the calendar
    Switch allDaySwitch;
    Calendar startDateTime, endDateTime, reminderDateTime; // This is the one that goes CAWrapper

    // Helps us format our dates later
    SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);

    // Helps with dynamic spinners
    ArrayAdapter<String> calendarSpinnerAdapter;
    List<CalbiticaCalendar> calbiticaCalendarsList;
    String existingCalendarID = "";
    List<String> calbiticaCalendarSummaries = new ArrayList<>();

    // Keeps track of Event's Mongo ID
    String _id = "";
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

        calbiticaCalendarSummaries.add("");

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
            System.out.println("mongo id " + _id);
            // EDITING: populate the views accordingly
            title.setText(bundle.getString("title"));
            allDaySwitch.setChecked(bundle.getBoolean("legitAllDay"));
//            existingCalendarID = bundle.getString("calendarID");
//          reminderDateTime = bundle.getString("reminderDateTime", mongoReminder);
            navTitleTV.setText("Edit Event");
        } else {
            reminderDateTime = null;
            navTitleTV.setText("Create Event");
        }

        String startDT = bundle.getString("startDateTime");
        String endDT = bundle.getString("endDateTime");
        startDateTime = Calendar.getInstance();
        endDateTime = Calendar.getInstance();

        // From the plus icon from NavigationBar
        try {
            Date startDTDate = (startDT.equals("")) ? new Date() : sdf.parse(startDT);
            Date endDTDate = (endDT.equals("")) ? new Date() : sdf.parse(endDT);

            startDateTime.setTime(startDTDate);
            endDateTime.setTime(endDTDate);

            // Default startDateTime will be automatically configure
            startDate.setText(DateUtil.ddMMMyyyy(startDTDate));
            startTime.setText(DateUtil.HHmm(startDTDate));
            endDate.setText(DateUtil.ddMMMyyyy(endDTDate));
            endTime.setText(DateUtil.HHmm(endDTDate));

        } catch (java.text.ParseException e) {
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
        final TextView reminderDate = (TextView) findViewById(R.id.reminderDate);
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

                TimePickerDialog timePickerDialog = new TimePickerDialog(WeekSaveEvent.this,
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
                        }, hourOfDay, minute, android.text.format.DateFormat.is24HourFormat(WeekSaveEvent.this));
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

    public void setupCalendarSpinner() {
        // should delegate to a ui thread
        CAWrapper.getAllCalendars(this, WeekSaveEvent.this);
        calendarSpinnerAdapter = new ArrayAdapter<String>(WeekSaveEvent.this, R.layout.spinner_item,
                calbiticaCalendarSummaries);

        calendarSpinnerAdapter.setDropDownViewResource(R.layout.spinner_item);
        calendarSpinner.setAdapter(calendarSpinnerAdapter);
    }

    // dynamic spinner
    public void onCalendarListResult(List<CalbiticaCalendar> calbiticaCalendars) {
        calbiticaCalendarsList = calbiticaCalendars;
        calbiticaCalendarSummaries.clear();

        for (CalbiticaCalendar c : calbiticaCalendars)
            calbiticaCalendarSummaries.add(c.getSummary());

        calendarSpinnerAdapter.notifyDataSetChanged();
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


                if (NavigationBar.selectedPages == "nav_week") {
                    System.out.println("INDEX: " + wveIndex);
                    System.out.println("INDEX: " + WeekFragment.mNewEvents.size());
                    System.out.println("INDEX: " + WeekFragment.listOfCalbits.size());
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
                        WeekFragment.weekView.notifyDatasetChanged();
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
                        AgendaFragment.agendaView.init(AgendaFragment.eventList, AgendaFragment.minDate, AgendaFragment.maxDate, Locale.getDefault(), AgendaFragment.calendarPickerController);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // Make Post Request to Calbitica API
                HashMap<String, String> calbit = new HashMap<>();
                Boolean isAllDay = allDaySwitch.isChecked();
                String startDateStr = isAllDay ? DateUtil.localToUTCAllDay(startDateTime.getTime())
                        : DateUtil.localToUTC(startDateTime.getTime());
                String endDateStr = isAllDay ? DateUtil.localToUTCAllDay(endDateTime.getTime())
                        : DateUtil.localToUTC(endDateTime.getTime());

                calbit.put("start", startDateStr);
                calbit.put("end", endDateStr);

                calbit.put("allDay", "" + isAllDay); // TODO: don't hardcode this please

                String calendarSummary = calendarSpinner.getSelectedItem().toString();
                String calendarID = "";
                if(calbiticaCalendarsList != null) {
                    for (CalbiticaCalendar c : calbiticaCalendarsList) {
                        if (c.getSummary().equals(calendarSummary)) {
                            calendarID = c.getGoogleID();
                            break;
                        }
                    }
                }

                calbit.put("calendarID", calendarID); // TODO: don't hardcode this please
                calbit.put("display", "true");
                calbit.put("isDump", "false");
                calbit.put("title", title.getText().toString());

                // TODO: Save reminders
                // TODO: Add location

                saveCalbit(calbit);
            }
        }

        return super.onOptionsItemSelected(item);

    }

    // TODO: disable time if on all day


    // API calls
    public void saveCalbit(HashMap<String, String> calbit) {
        // Retrieve the JWT
        String oldJWT = UserData.get("jwt", WeekSaveEvent.this);

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
                        System.out.println(calbit);
                        Log.d("SAVE CALL", response.errorBody().string());
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

                        UserData.save(user, WeekSaveEvent.this);
                    }

                    // Close this activity!
                    String successTxt = isEditing ? "Event updated." : "Event created.";
                    Toast.makeText(WeekSaveEvent.this, successTxt, Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                } catch (Exception e) {
                    Log.d("API JWT FAILED", e.getLocalizedMessage());
                    setResult(Activity.RESULT_CANCELED);
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
