package com.calbitica.app.Week_Calendar;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.calbitica.app.R;

public class Week_EditEvent extends AppCompatActivity {
    EditText eventTitle = null;
    JSONObject colorInfo = new JSONObject();
    Calendar startDateTime, endDateTime = null;
    WeekViewEvent event = null;
    com.calbitica.app.Database.Firebase firebase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Using the same layout of the Event Create
        setContentView(R.layout.activity_week__create_event);

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
        startDateTime = Calendar.getInstance();
        endDateTime = Calendar.getInstance();
        final int color = bundle.getInt("color");

        try{
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            startDateTime.setTime(sdf.parse(startDT));
            endDateTime.setTime(sdf.parse(endDT));
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        // Default eventTitle will be automatically configure
        eventTitle = findViewById(R.id.title);
        eventTitle.setText(title);

        final Spinner eventColor = (Spinner) findViewById(R.id.color);

        // Get the colorPosition from the firebase, as selected on default
        DatabaseReference firebase = FirebaseDatabase.getInstance().getReference().child("Calbitica").child("Calendar");
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, String> eventData = (Map<String, String>) dataSnapshot.getValue();
                    for (String key : eventData.keySet()) {
                        Object data = eventData.get(key);

                        try {
                            HashMap<String, Object> eventObject = (HashMap<String, Object>) data;

                            // Getting the JSON Object from colorInfo, based on key
                            HashMap<String, Object> colorObject = (HashMap<String, Object>) eventObject.get("colorInfo");

                            // Check and render the firebase with the existing data(Only 1 data will be found)
                            String firebaseEvent = colorObject.get("color").toString();
                            int firebaseColor = Integer.parseInt(firebaseEvent);
                            if(firebaseColor == color) {
                                String colorPosition = colorObject.get("colorPosition").toString();
                                int position = Integer.parseInt(colorPosition);
                                eventColor.setSelection(position);
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

        // When selected the Spinner drop-down, the background color will change accordingly
        eventColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        parent.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.c_teal_1));
                        try {
                            colorInfo.put("color", getResources().getColor(R.color.c_teal_1));
                            colorInfo.put("colorText", "Teal 1");
                            colorInfo.put("colorPosition", 0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 1:
                        parent.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.c_teal_2));
                        try {
                            colorInfo.put("color", getResources().getColor(R.color.c_teal_2));
                            colorInfo.put("colorText", "Teal 2");
                            colorInfo.put("colorPosition", 1);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 2:
                        parent.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.c_orange_1));
                        try {
                            colorInfo.put("color", getResources().getColor(R.color.c_orange_1));
                            colorInfo.put("colorText", "Orange 1");
                            colorInfo.put("colorPosition", 2);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 3:
                        parent.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.c_orange_2));
                        try {
                            colorInfo.put("color", getResources().getColor(R.color.c_orange_2));
                            colorInfo.put("colorText", "Orange 2");
                            colorInfo.put("colorPosition", 3);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 4:
                        parent.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.c_blue_1));
                        try {
                            colorInfo.put("color", getResources().getColor(R.color.c_blue_1));
                            colorInfo.put("colorText", "Blue 1");
                            colorInfo.put("colorPosition", 4);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 5:
                        parent.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.c_blue_2));
                        try {
                            colorInfo.put("color", getResources().getColor(R.color.c_blue_2));
                            colorInfo.put("colorText", "Blue 2");
                            colorInfo.put("colorPosition", 5);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 6:
                        parent.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.c_purple_1));
                        try {
                            colorInfo.put("color", getResources().getColor(R.color.c_purple_1));
                            colorInfo.put("colorText", "Purple 1");
                            colorInfo.put("colorPosition", 6);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 7:
                        parent.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.c_purple_2));
                        try {
                            colorInfo.put("color", getResources().getColor(R.color.c_purple_2));
                            colorInfo.put("colorText", "Purple 2");
                            colorInfo.put("colorPosition", 7);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 8:
                        parent.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.c_pink_1));
                        try {
                            colorInfo.put("color", getResources().getColor(R.color.c_pink_1));
                            colorInfo.put("colorText", "Pink 1");
                            colorInfo.put("colorPosition", 8);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 9:
                        parent.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.c_pink_2));
                        try {
                            colorInfo.put("color", getResources().getColor(R.color.c_pink_2));
                            colorInfo.put("colorText", "Pink 2");
                            colorInfo.put("colorPosition", 9);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 10:
                        parent.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.c_red_1));
                        try {
                            colorInfo.put("color", getResources().getColor(R.color.c_red_1));
                            colorInfo.put("colorText", "Red 1");
                            colorInfo.put("colorPosition", 10);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 11:
                        parent.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.c_red_2));
                        try {
                            colorInfo.put("color", getResources().getColor(R.color.c_red_2));
                            colorInfo.put("colorText", "Red 2");
                            colorInfo.put("colorPosition", 11);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 12:
                        parent.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.c_green_1));
                        try {
                            colorInfo.put("color", getResources().getColor(R.color.c_green_1));
                            colorInfo.put("colorText", "Green 1");
                            colorInfo.put("colorPosition", 12);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 13:
                        parent.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.c_green_2));
                        try {
                            colorInfo.put("color", getResources().getColor(R.color.c_green_2));
                            colorInfo.put("colorText", "Green 2");
                            colorInfo.put("colorPosition", 13);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // When no selected, it will be default color
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

                DatePickerDialog datePickerDialog = new DatePickerDialog(Week_EditEvent.this, new DatePickerDialog.OnDateSetListener() {
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

                TimePickerDialog timePickerDialog = new TimePickerDialog(Week_EditEvent.this, new TimePickerDialog.OnTimeSetListener() {
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
                }, hourOfDay, minute, android.text.format.DateFormat.is24HourFormat(Week_EditEvent.this));
                timePickerDialog.show();
            }
        });

        // Default startDateTime will be automatically configure
        int startMonth = startDateTime.get(Calendar.MONTH) + 1;

        if(startDateTime.getTime() != null) {
            startDate.setText(startDateTime.get(Calendar.DAY_OF_MONTH) + "/" + startMonth + "/" + startDateTime.get(Calendar.YEAR));
            startTime.setText(startDateTime.get(Calendar.HOUR) + ":" + "0" + startDateTime.get(Calendar.MINUTE));
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

                DatePickerDialog datePickerDialog = new DatePickerDialog(Week_EditEvent.this, new DatePickerDialog.OnDateSetListener() {
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

                TimePickerDialog timePickerDialog = new TimePickerDialog(Week_EditEvent.this, new TimePickerDialog.OnTimeSetListener() {
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
                }, hourOfDay, minute, android.text.format.DateFormat.is24HourFormat(Week_EditEvent.this));
                timePickerDialog.show();
            }
        });

        // Default endDateTime will be automatically configure
        int endMonth = endDateTime.get(Calendar.MONTH) + 1;

        if(endDateTime.getTime() != null) {
            endDate.setText(endDateTime.get(Calendar.DAY_OF_MONTH) + "/" + endMonth + "/" + endDateTime.get(Calendar.YEAR));
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
            if(eventTitle.getText().toString().equals("")) {
                Toast.makeText(Week_EditEvent.this,"Please enter your title", Toast.LENGTH_SHORT).show();
            } else if (startDateTime.getTime().getTime() >= endDateTime.getTime().getTime()) {
                // Making use of the Epoch & Unix Timestamp Conversion Tools, can easily tell all the information of the dates
                Toast.makeText(Week_EditEvent.this,"Start DateTime cannot be more than or equal to End DateTime", Toast.LENGTH_SHORT).show();
            } else {
                Bundle bundle = getIntent().getExtras();
                Long id = bundle.getLong("id");

                // Modify event with new data(Only 1 data will be found and modify)
                for(WeekViewEvent event : WeekFragment.mNewEvents) {
                    if(event.getId() == id) {
                        event.setName(eventTitle.getText().toString());
                        event.setStartTime(startDateTime);
                        event.setEndTime(endDateTime);
                        try {
                            int colorText = (Integer) colorInfo.get("color");
                            event.setColor(colorText);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Update in Firebase with the new data
                firebase = new com.calbitica.app.Database.Firebase();
                firebase.updateWeekEventInFirebase(id, eventTitle.getText().toString(), startDateTime.getTime().toString(), endDateTime.getTime().toString(), colorInfo);

                // Refresh the week view. onMonthChange will be called again.
                WeekFragment.weekView.notifyDatasetChanged();
                finish();
                Toast.makeText(Week_EditEvent.this,"Event successfully updated", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
