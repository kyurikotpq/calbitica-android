package com.calbitica.app.Week;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import com.calbitica.app.Util.CAWrapper;
import com.calbitica.app.NavigationBar.NavigationBar;
import com.calbitica.app.Agenda.AgendaFragment;
import com.github.tibolte.agendacalendarview.models.BaseCalendarEvent;

import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.calbitica.app.R;

public class WeekEditEvent extends AppCompatActivity {
    EditText eventTitle = null;                                     // The iuput calendar title
    JSONObject colorInfo = new JSONObject();                        // To make it more information and more easier
    Calendar startDateTime, endDateTime, reminderDateTime = null;   // The input calendar start and end datetime
    WeekViewEvent event = null;                                     // The events that will in Week CalbiticaCalendar
    ArrayList<String> calendarArrayKey = new ArrayList<>();         // Using this to tally with the specific calendar value
    CAWrapper CAWrapper = null;                                       // Reference for tally with the CAWrapper

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
/*
        // Get the _id from the CAWrapper, as for valid checking
        CAWrapper.getAllCalbit(WeekEditEvent.this);

        // Using the same layout of the Event Create
        setContentView(R.layout.activity_week__create_event);

        // Get the _id from the database, as for valid checking
        Database database = new Database(WeekEditEvent.this);
        database.getAllCalbit();

        // Default the text will be Calbitica Android, by setting as empty for custom TextView to be shown instead
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        TextView nav_title = (TextView) findViewById(R.id.nav_Title);
        nav_title.setText("Editing Event");

        // When click the cross image, will go back to WeekFragment
        ImageView close = (ImageView) findViewById(R.id.nav_Close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Get info from WeekFragment(id will be using as the edit portion in onOptionsItemSelected)
        Bundle bundle = getIntent().getExtras();
        String title = bundle.getString("title");
        String startDT = bundle.getString("startDateTime");
        String endDT = bundle.getString("endDateTime");
        String reminderDT = bundle.getString("reminderDateTime");
        startDateTime = Calendar.getInstance();
        endDateTime = Calendar.getInstance();
        reminderDateTime = Calendar.getInstance();

        try{
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            startDateTime.setTime(sdf.parse(startDT));
            endDateTime.setTime(sdf.parse(endDT));

            if(reminderDT.length() != 0 || reminderDT != null) {
                reminderDateTime.setTime(sdf.parse(reminderDT));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Default eventTitle will be automatically configure
        eventTitle = findViewById(R.id.title);
        eventTitle.setText(title);

        final Spinner eventSync = (Spinner) findViewById(R.id.selectCalendar);
        ArrayList<String> calendarArrayValue = new ArrayList<>();

        CAWrapper data = new CAWrapper(getBaseContext());
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

        // When selected the Spinner drop-down, the background color will change accordingly
        eventSync.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String tutorialsName = parent.getItemAtPosition(position).toString();
                Toast.makeText(parent.getContext(), "Selected: " + tutorialsName, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // When no selected, it will default
            }
        });

        // Prompt the Start Date Picker to choose
        final TextView startDate = (TextView) findViewById(R.id.startDate);
        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(WeekEditEvent.this, new DatePickerDialog.OnDateSetListener() {
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
        final TextView startTime = (TextView) findViewById(R.id.startTime);
        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(WeekEditEvent.this, new TimePickerDialog.OnTimeSetListener() {
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
                }, hourOfDay, minute, android.text.format.DateFormat.is24HourFormat(WeekEditEvent.this));
                timePickerDialog.show();
            }
        });

        // Default startDateTime will be automatically configure
        int startMonth = startDateTime.get(Calendar.MONTH) + 1;

        if(startDateTime.getTime() != null) {
            startDate.setText(startDateTime.get(Calendar.DAY_OF_MONTH) + "/" + startMonth + "/" + startDateTime.get(Calendar.YEAR));

            if(startDateTime.get(Calendar.MINUTE) < 10) {
                startTime.setText(startDateTime.get(Calendar.HOUR_OF_DAY) + ":" + "0" + startDateTime.get(Calendar.MINUTE));
            } else {
                startTime.setText(startDateTime.get(Calendar.HOUR_OF_DAY) + ":" + startDateTime.get(Calendar.MINUTE));
            }
        }

        // Prompt the End Date Picker to choose
        final TextView endDate = (TextView) findViewById(R.id.endDate);
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(WeekEditEvent.this, new DatePickerDialog.OnDateSetListener() {
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
        final TextView endTime = (TextView) findViewById(R.id.endTime);
        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(WeekEditEvent.this, new TimePickerDialog.OnTimeSetListener() {
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
                }, hourOfDay, minute, android.text.format.DateFormat.is24HourFormat(WeekEditEvent.this));
                timePickerDialog.show();
            }
        });

        // Default endDateTime will be automatically configure
        int endMonth = endDateTime.get(Calendar.MONTH) + 1;

        if(endDateTime.getTime() != null) {
            endDate.setText(endDateTime.get(Calendar.DAY_OF_MONTH) + "/" + endMonth + "/" + endDateTime.get(Calendar.YEAR));

            if(endDateTime.get(Calendar.MINUTE) < 10) {
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

                DatePickerDialog datePickerDialog = new DatePickerDialog(WeekEditEvent.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        reminderDateTime.set(year, month, day);
                        reminderDate.setText(day + "/" + (month + 1) + "/" + year);
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

                TimePickerDialog timePickerDialog = new TimePickerDialog(WeekEditEvent.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        if(minute < 10) {
                            reminderTime.setText(hourOfDay + ":" + "0" + minute);
                        } else {
                            reminderTime.setText(hourOfDay + ":" + minute);
                        }

                        reminderDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        reminderDateTime.set(Calendar.MINUTE, minute);
                    }
                }, hourOfDay, minute, android.text.format.DateFormat.is24HourFormat(WeekEditEvent.this));
                timePickerDialog.show();
            }
        });

        // Default reminderDateTime will be automatically configure
        int reminderMonth = reminderDateTime.get(Calendar.MONTH) + 1;

        if(reminderDateTime.getTime() != null) {
            reminderDate.setText(reminderDateTime.get(Calendar.DAY_OF_MONTH) + "/" + reminderMonth + "/" + reminderDateTime.get(Calendar.YEAR));

            if(reminderDateTime.get(Calendar.MINUTE) < 10) {
                reminderTime.setText(reminderDateTime.get(Calendar.HOUR_OF_DAY) + ":" + "0" + reminderDateTime.get(Calendar.MINUTE));
            } else {
                reminderTime.setText(reminderDateTime.get(Calendar.HOUR_OF_DAY) + ":" + reminderDateTime.get(Calendar.MINUTE));
            }
        }
        */
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
        /*
        if (item.getItemId() == R.id.ok) {
            if(eventTitle.getText().toString().equals("")) {
                Toast.makeText(WeekEditEvent.this,"Please enter your title", Toast.LENGTH_SHORT).show();
            } else if (startDateTime.getTime().getTime() >= endDateTime.getTime().getTime()) {
                // Making use of the Epoch & Unix Timestamp Conversion Tools, can easily tell all the information of the dates
                Toast.makeText(WeekEditEvent.this,"Start DateTime cannot be more than or equal to End DateTime", Toast.LENGTH_SHORT).show();
            } else {
                // Setting the valid mongoId for the reference with the CAWrapper
                int id = (int) event.getId();

                if(NavigationBar.selectedPages == "nav_week") {
                    // Modify event with new data(Only 1 data will be found and modify)
                    for(WeekViewEvent event : WeekFragment.mNewEvents) {
                        if(CAWrapper.getAllCalbit().get(id) != null) {
                            event.setName(eventTitle.getText().toString());
                            event.setStartTime(startDateTime);
                            event.setEndTime(endDateTime);

                            // Refresh the week view. onMonthChange will be called again.
                            WeekFragment.weekView.notifyDatasetChanged();
                        }
                    }
<<<<<<< HEAD
                } else if (NavigationBar.selectedPages == "nav_schedule") {
                    // First, I delete the Schedule CalbiticaCalendar selected event(Only 1), due to BaseCalendarEvent options is inside CalendarEvent(only color is not in the list, so...)
=======
                } else if (NavigationBar.selectedPages == "nav_agenda") {
                    // First, I delete the Agenda Calendar selected event(Only 1), due to BaseCalendarEvent options is inside CalendarEvent(only color is not in the list, so...)
>>>>>>> 211b6f1a3ca827fcf296fc4ace53dc290e53fb72
                    // Secondly, then I add again with the updated values, so will still serve as the edit portion...
                    for(int i = 0; i < AgendaFragment.eventList.size(); i++) {
                        if(AgendaFragment.eventList.get(i).getId() == id) {
                            AgendaFragment.eventList.remove(i);   // remove only 1

                            BaseCalendarEvent allEvent = new BaseCalendarEvent(eventTitle.getText().toString(), "", "", 0, startDateTime, endDateTime, false);
                            allEvent.setId(id);
                            AgendaFragment.eventList.add(allEvent);

                            // Schedule CalbiticaCalendar will also re-render the events as well
                            AgendaFragment.scheduleView.init(AgendaFragment.eventList, AgendaFragment.minDate, AgendaFragment.maxDate, Locale.getDefault(), AgendaFragment.calendarPickerController);
                        }
                    }
                }

                Date start = null, end = null, reminder = null;

                try{
                    SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-DDTHH:mm:ssZ", Locale.ENGLISH);
                    start = sdf.parse(startDateTime.getTime().toString());
                    end = sdf.parse(endDateTime.getTime().toString());

                    if(reminderDateTime.getTime() != null) {
                        reminder = sdf.parse(reminderDateTime.getTime().toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

//                CAWrapper.updateEventInCalbit(CAWrapper.getAllCalbit().get(id).get_id().toString(), eventTitle.getText().toString(), start, end, reminder);

                finish();
                Toast.makeText(WeekEditEvent.this,"Event successfully updated", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);

    */
        return true;
    }
}
