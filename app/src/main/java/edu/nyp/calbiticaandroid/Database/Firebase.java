package edu.nyp.calbiticaandroid.Database;

import com.alamkanak.weekview.WeekViewEvent;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import androidx.annotation.NonNull;
import edu.nyp.calbiticaandroid.WeekFragment;

public class Firebase {
    // Necessary fields for Week Calendar Event
    long calendarID;
    String title, startDateTime, endDateTime, colorText;

    public Firebase() {
    }

    public long getCalendarID() {
        return calendarID;
    }

    public String getTitle() {
        return title;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public String getColorText() {
        return colorText;
    }

    public void setCalendarID(long calendarID) {
        this.calendarID = calendarID;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }

    public void setColorText(String colorText) {
        this.colorText = colorText;
    }

    // Get the events from firebase
    public void getEventsFromFirebase() {
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

                            long calendarID = (long) eventObject.get("calendarID");
                            String title = eventObject.get("title").toString();
                            Calendar startDateTime = Calendar.getInstance();
                            Calendar endDateTime = Calendar.getInstance();

                            String stringStartDateTime = (String) eventObject.get("startDateTime");
                            String stringEndDateTime = (String) eventObject.get("endDateTime");

                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                                startDateTime.setTime(sdf.parse(stringStartDateTime));
                                endDateTime.setTime(sdf.parse(stringEndDateTime));
                            } catch (java.text.ParseException e) {
                                e.printStackTrace();
                            }

                            String colorText = (String) eventObject.get("colorText");
                            int color = Integer.parseInt(colorText);

                            WeekViewEvent events = new WeekViewEvent(calendarID, title, startDateTime, endDateTime);
                            events.setColor(color);
                            WeekFragment.mNewEvents.add(events);
                            WeekFragment.weekView.notifyDatasetChanged();
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
    }

    // Save in Firebase
    public void saveInFirebase(long calendarID, String title, String startDateTime, String endDateTime, String colorText) {
        Firebase data = new Firebase();

        data.setCalendarID(calendarID);
        data.setTitle(title);
        data.setStartDateTime(startDateTime);
        data.setEndDateTime(endDateTime);
        data.setColorText(colorText);

        DatabaseReference firebase = FirebaseDatabase.getInstance().getReference().child("Calbitica").child("Calendar");
        firebase.push().setValue(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                System.out.println("Event added to database");
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("Event fail to add into database");
            }
        });
    }
}
