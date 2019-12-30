package edu.nyp.calbiticaandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.alamkanak.weekview.WeekViewEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class Week_CreateEvent extends AppCompatActivity {
    Toolbar toolbar = null;
    ImageView close = null;
    EditText title = null;
    Spinner color = null;
    int colorText = 0;
    TextView startDate, startTime, endDate, endTime = null;
    Calendar startDateTime, endDateTime = null;
    WeekViewEvent event = null;
    edu.nyp.calbiticaandroid.Database.Firebase firebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.week__create_event);

        title = findViewById(R.id.title);

        // Get info from WeekFragment
        Bundle bundle = getIntent().getExtras();
        String startDT = bundle.getString("startDateTime");
        String endDT = bundle.getString("endDateTime");
        startDateTime = Calendar.getInstance();
        endDateTime = Calendar.getInstance();

        try{
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            startDateTime.setTime(sdf.parse(startDT));
            endDateTime.setTime(sdf.parse(endDT));
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        // Default the text will be Calbitica Android, by setting as empty for custom TextView to be shown instead
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        // When click the cross image, will go back to WeekFragment
        close = findViewById(R.id.nav_Close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // When selected the drop-down, the background color will change accordingly
        color = findViewById(R.id.color);
        color.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        parent.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.c_teal_1));
                        colorText = getResources().getColor(R.color.c_teal_1);
                        break;
                    case 1:
                        parent.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.c_teal_2));
                        colorText = getResources().getColor(R.color.c_teal_2);
                        break;
                    case 2:
                        parent.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.c_orange_1));
                        colorText = getResources().getColor(R.color.c_orange_1);
                        break;
                    case 3:
                        parent.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.c_orange_2));
                        colorText = getResources().getColor(R.color.c_orange_2);
                        break;
                    case 4:
                        parent.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.c_blue_1));
                        colorText = getResources().getColor(R.color.c_blue_1);
                        break;
                    case 5:
                        parent.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.c_blue_2));
                        colorText = getResources().getColor(R.color.c_blue_2);
                        break;
                    case 6:
                        parent.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.c_purple_1));
                        colorText = getResources().getColor(R.color.c_purple_1);
                        break;
                    case 7:
                        parent.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.c_purple_2));
                        colorText = getResources().getColor(R.color.c_purple_2);
                        break;
                    case 8:
                        parent.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.c_pink_1));
                        colorText = getResources().getColor(R.color.c_pink_1);
                        break;
                    case 9:
                        parent.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.c_pink_2));
                        colorText = getResources().getColor(R.color.c_pink_2);
                        break;
                    case 10:
                        parent.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.c_red_1));
                        colorText = getResources().getColor(R.color.c_red_1);
                        break;
                    case 11:
                        parent.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.c_red_2));
                        colorText = getResources().getColor(R.color.c_red_2);
                        break;
                    case 12:
                        parent.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.c_green_1));
                        colorText = getResources().getColor(R.color.c_green_1);
                        break;
                    case 13:
                        parent.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.c_green_2));
                        colorText = getResources().getColor(R.color.c_green_2);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // When no selected, it will be default color
            }
        });

        // Prompt the Start Date Picker to choose
        startDate = findViewById(R.id.startDate);
        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(Week_CreateEvent.this, new DatePickerDialog.OnDateSetListener() {
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
        startTime = findViewById(R.id.startTime);
        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(Week_CreateEvent.this, new TimePickerDialog.OnTimeSetListener() {
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
                }, hourOfDay, minute, android.text.format.DateFormat.is24HourFormat(Week_CreateEvent.this));
                timePickerDialog.show();
            }
        });

        // Default startDateTime will be automatically configure
        int startMonth = startDateTime.get(Calendar.MONTH) + 1;

        if(startDateTime.getTime() != null) {
            startDate.setText(startDateTime.get(Calendar.DAY_OF_MONTH) + "/"
                    + startMonth + "/" + startDateTime.get(Calendar.YEAR));

            startTime.setText(startDateTime.get(Calendar.HOUR) + ":" + "0" + startDateTime.get(Calendar.MINUTE));
        }

        // Prompt the End Date Picker to choose
        endDate = findViewById(R.id.endDate);
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(Week_CreateEvent.this, new DatePickerDialog.OnDateSetListener() {
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
        endTime = findViewById(R.id.endTime);
        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(Week_CreateEvent.this, new TimePickerDialog.OnTimeSetListener() {
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
                }, hourOfDay, minute, android.text.format.DateFormat.is24HourFormat(Week_CreateEvent.this));
                timePickerDialog.show();
            }
        });

        // Default endDateTime will be automatically configure
        int endMonth = endDateTime.get(Calendar.MONTH) + 1;

        if(endDateTime.getTime() != null) {
            endDate.setText(endDateTime.get(Calendar.DAY_OF_MONTH) + "/"
                    + endMonth + "/" + endDateTime.get(Calendar.YEAR));

            endTime.setText(endDateTime.get(Calendar.HOUR) + ":" + "0" + endDateTime.get(Calendar.MINUTE));
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
            if(title.getText().toString().equals("")) {
                Toast.makeText(Week_CreateEvent.this,"Please enter your title", Toast.LENGTH_SHORT).show();
            } else {
                long calendarID = (UUID.randomUUID().getMostSignificantBits());

                // Create a new event.
                // calendarID -> random generate long id(unique), name -> event title
                event = new WeekViewEvent(calendarID, title.getText().toString(), startDateTime, endDateTime);
                event.setColor(colorText);
                WeekFragment.mNewEvents.add(event);

                // Save in Firebase
                firebase = new edu.nyp.calbiticaandroid.Database.Firebase();
                firebase.saveInFirebase(calendarID, title.getText().toString(), startDateTime.getTime().toString(), endDateTime.getTime().toString(), String.valueOf(colorText));

                // Refresh the week view. onMonthChange will be called again.
                WeekFragment.weekView.notifyDatasetChanged();
                finish();
                Toast.makeText(Week_CreateEvent.this,"Event successfully created", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
