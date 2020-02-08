package com.calbitica.app.Week;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.calbitica.app.Database.Database;
import com.calbitica.app.NavigationBar.NavigationBar;
import com.calbitica.app.R;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.alamkanak.weekview.WeekViewEvent;
import com.calbitica.app.Agenda.AgendaFragment;
import com.github.tibolte.agendacalendarview.models.BaseCalendarEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class WeekCreateEvent extends AppCompatActivity {
    EditText title = null;                                  // Input Calendar Title
    TextView startDate, startTime, endDate, endTime;        // This is just the display from the layout
    Calendar startDateTime, endDateTime;                    // This is the one that goes database
    WeekViewEvent event = null;                             // The events that will in Week Calendar
    private Database database = null;                       // Reference for tally with the database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week__create_event);

        // Get the _id from the database, as for valid checking
        Database database = new Database(WeekCreateEvent.this);
        database.getAllCalbit();

        title = findViewById(R.id.title);

        // Default the text will be Calbitica Android, by setting as empty for custom TextView to be shown instead
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

        // From the plus icon from NavigationBar
        if(!startDT.equals("") && !endDT.equals("")) {
            try{
                SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                startDateTime.setTime(sdf.parse(startDT));
                endDateTime.setTime(sdf.parse(endDT));
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
        }

        final Spinner eventSync = (Spinner) findViewById(R.id.selectCalendar);
        ArrayList<String> calendarArrayValue = new ArrayList<>();
        ArrayList<String> calendarArrayKey = new ArrayList<>();         // Using this to tally with the specific calendar value

        Database data = new Database(getBaseContext());
        data.getAllCalendars();

        Toast.makeText(getBaseContext(), "Please wait for Google Account to render...", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < data.getAllCalendars().size(); i++) {
                    calendarArrayKey.add(data.getAllCalendars().get(i).getGoogleID());
                    calendarArrayValue.add(data.getAllCalendars().get(i).getSummary());
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, calendarArrayValue);
                    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    eventSync.setAdapter(arrayAdapter);
                    eventSync.setBackgroundColor(getResources().getColor(R.color.c_teal_1));
                }
            }
        }, 3000);

        eventSync.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String tutorialsName = parent.getItemAtPosition(position).toString();
                Toast.makeText(parent.getContext(), "Selected: " + tutorialsName, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // When no selected, it will be default color
            }
        });

        // Prompt the Start Date Picker to choose
        startDate = (TextView) findViewById(R.id.startDate);
        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(WeekCreateEvent.this, new DatePickerDialog.OnDateSetListener() {
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

                TimePickerDialog timePickerDialog = new TimePickerDialog(WeekCreateEvent.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        if(minute < 10) {
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
        if(!startDT.equals("")) {
            int startMonth = startDateTime.get(Calendar.MONTH) + 1;
            startDate.setText(startDateTime.get(Calendar.DAY_OF_MONTH) + "/" + startMonth + "/" + startDateTime.get(Calendar.YEAR));

            if(startDateTime.get(Calendar.MINUTE) < 10) {
                startTime.setText(startDateTime.get(Calendar.HOUR_OF_DAY) + ":" + "0" + startDateTime.get(Calendar.MINUTE));
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

                DatePickerDialog datePickerDialog = new DatePickerDialog(WeekCreateEvent.this, new DatePickerDialog.OnDateSetListener() {
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

                TimePickerDialog timePickerDialog = new TimePickerDialog(WeekCreateEvent.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        if(minute < 10) {
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
        if(!endDT.equals("")) {
            int endMonth = endDateTime.get(Calendar.MONTH) + 1;
            endDate.setText(endDateTime.get(Calendar.DAY_OF_MONTH) + "/" + endMonth + "/" + endDateTime.get(Calendar.YEAR));

            if(endDateTime.get(Calendar.MINUTE) < 10) {
                endTime.setText(endDateTime.get(Calendar.HOUR_OF_DAY) + ":" + "0" + endDateTime.get(Calendar.MINUTE));
            } else {
                endTime.setText(endDateTime.get(Calendar.HOUR_OF_DAY) + ":" + endDateTime.get(Calendar.MINUTE));
            }
        }
    }

    // Right Menu Bar Creation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.event_menu, menu);
        return true;
    }

    // Right Menu Bar Selected, which is the Tick Image
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.ok) {
            // Due to Agenda Calendar "No events" is a empty view as default
            if(title.getText().toString().equals("") || title.getText().toString().equals("No events") ||
               startDate.getText().toString().equals("") || startTime.getText().toString().equals("") ||
               endDate.getText().toString().equals("") || endTime.getText().toString().equals("")) {
                Toast.makeText(WeekCreateEvent.this,"Please fill in all the fields", Toast.LENGTH_SHORT).show();
            } else if (startDateTime.getTime().getTime() >= endDateTime.getTime().getTime()) {
                // Making use of the Epoch & Unix Timestamp Conversion Tools, can easily tell all the information of the dates
                Toast.makeText(WeekCreateEvent.this,"Start DateTime cannot be more than or equal to End DateTime", Toast.LENGTH_SHORT).show();
            } else {
                // calendarID -> random generate long id(unique), to represent the specific unique events
                long calendarID = (UUID.randomUUID().getMostSignificantBits());

                if(NavigationBar.selectedPages == "nav_week") {
                    // Create a new event for Week Calendar
                    event = new WeekViewEvent(calendarID, title.getText().toString(), startDateTime, endDateTime);
                    event.setColor(Color.rgb(100, 200, 220));
                    WeekFragment.mNewEvents.add(event);

                    // Refresh the week view. onMonthChange will be called again.
                    WeekFragment.weekView.notifyDatasetChanged();
                } else if (NavigationBar.selectedPages == "nav_agenda") {
                    // Create a new event for Agenda Calendar
                    BaseCalendarEvent allEvent = new BaseCalendarEvent(title.getText().toString(), "", "", Color.rgb(100, 200, 220), startDateTime, endDateTime, false);
                    allEvent.setId(calendarID);
                    AgendaFragment.eventList.add(allEvent);

                    // Agenda Calendar re-render the events and refresh the calendar
                    AgendaFragment.agendaView.init(AgendaFragment.eventList, AgendaFragment.minDate, AgendaFragment.maxDate, Locale.getDefault(), AgendaFragment.calendarPickerController);
                }

                // Save in Firebase, this will saved both calendar
//                firebase = new com.calbitica.app.Database.Firebase();
//                firebase.saveWeekEventInFirebase(calendarID, title.getText().toString(), startDateTime.getTime().toString(), endDateTime.getTime().toString(), colorInfo);

                finish();
                Toast.makeText(WeekCreateEvent.this,"Event successfully created", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
