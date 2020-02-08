package com.calbitica.app.Database;

import com.alamkanak.weekview.WeekViewEvent;
import com.calbitica.app.NavigationBar.NavigationBar;
import com.calbitica.app.Week.WeekFragment;
import com.github.tibolte.agendacalendarview.models.BaseCalendarEvent;
import com.github.tibolte.agendacalendarview.models.CalendarEvent;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import androidx.annotation.NonNull;

public class Firebase {
    // Necessary fields for Week Calendar Event
    long calendarID;
    String title, startDateTime, endDateTime;
    Map<String, Object> colorInfo;

    // Default Firebase Constructor
    public Firebase() {}

    // All Calendar Getter
    public long getCalendarID() { return calendarID; }
    public String getTitle() { return title; }
    public String getStartDateTime() { return startDateTime; }
    public String getEndDateTime() { return endDateTime; }
    public Map<String, Object> getColorInfo() { return colorInfo; }

    // All this set[name] -> the [name] will be the firebase key (Important)
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
    public void setColorInfo(Map<String, Object> colorInfo) {
        this.colorInfo = colorInfo;
    }

    // Get the week events from firebase, only render once(will be discard after that)
    public void getWeekEventsFromFirebase() {
        // Pin-point the location of the data that you want
        DatabaseReference firebase = FirebaseDatabase.getInstance().getReference().child(NavigationBar.acctName).child("Calbitica").child("Calendar");
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get the unique key for each, due to cannot pin-point the unique value
                    Map<String, String> eventData = (Map<String, String>) dataSnapshot.getValue();
                    for (String key : eventData.keySet()) {
                        Object data = eventData.get(key);

                        try {
                            // Retrieve as a object, easier to do the conversion
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

                            // Getting the JSON Object from colorInfo, based on key again
                            HashMap<String, Object> colorObject = (HashMap<String, Object>) eventObject.get("colorInfo");
                            String colorText = colorObject.get("color").toString();
                            int color = Integer.parseInt(colorText);

                            // Render all the firebase data into WeekView Calendar
                            WeekViewEvent weekEvents = new WeekViewEvent(calendarID, title, startDateTime, endDateTime);
                            weekEvents.setColor(color);
                            WeekFragment.mNewEvents.add(weekEvents);

                            // Refresh the Week Calendar
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

                    System.out.println("Events successfully retrieved from database");
                } else {
                    System.out.println("Fail to retrieve from database");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println(databaseError.getCode());
            }
        });
    }

    // Save week event in Firebase
    public void saveWeekEventInFirebase(long calendarID, String title, String startDateTime, String endDateTime, JSONObject colorInfo) {
        Firebase data = new Firebase();

        data.setCalendarID(calendarID);
        data.setTitle(title);
        data.setStartDateTime(startDateTime);
        data.setEndDateTime(endDateTime);

        // Set the JsonObject format to firebase, colorMap will be the value
        Map<String, Object> colorMap = new Gson().fromJson(colorInfo.toString(), new TypeToken<HashMap<String, Object>>() {}.getType());
        data.setColorInfo(colorMap);

        DatabaseReference firebase = FirebaseDatabase.getInstance().getReference().child(NavigationBar.acctName).child("Calbitica").child("Calendar");
        // push -> automatically create a firebase unique id
        firebase.push().setValue(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                System.out.println("Event added to database");
            }
        }) .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("Fail to add event into database");
            }
        });
    }

    // Update week event in Firebase with the new data
    public void updateWeekEventInFirebase(final long calendarID, final String title, final String startDateTime, final String endDateTime, final JSONObject colorInfo) {
        // Getting all the firebase data again to do the check and save the firebase id accordingly
        DatabaseReference firebase = FirebaseDatabase.getInstance().getReference().child(NavigationBar.acctName).child("Calbitica").child("Calendar");
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get the unique key for each, due to cannot pin-point the unique value
                    Map<String, String> eventData = (Map<String, String>) dataSnapshot.getValue();
                    for (String key : eventData.keySet()) {
                        Object data = eventData.get(key);
                        try {
                            // Retrieve as a object, easier to do the conversion
                            HashMap<String, Object> eventObject = (HashMap<String, Object>) data;

                            // Making use of calendarID to do the checking(Unique id also)
                            long eventID = (long) eventObject.get("calendarID");

                            // Check and render the firebase with the existing data(Only 1 data will be found)
                            if (eventID == calendarID) {
                                // firebaseID will be the parent of the key, which needed this to update
                                String firebaseID = eventData.getOrDefault(eventData.keySet(), key);

                                Firebase firebaseData = new Firebase();

                                firebaseData.setCalendarID(calendarID);
                                firebaseData.setTitle(title);
                                firebaseData.setStartDateTime(startDateTime);
                                firebaseData.setEndDateTime(endDateTime);

                                // Set the JsonObject format to firebase, colorMap will be the value
                                Map<String, Object> colorMap = new Gson().fromJson(colorInfo.toString(), new TypeToken<HashMap<String, Object>>() {}.getType());
                                firebaseData.setColorInfo(colorMap);

                                // Adding one more child according to the parent key, to change the respective update values
                                DatabaseReference fire = FirebaseDatabase.getInstance().getReference().child(NavigationBar.acctName).child("Calbitica").child("Calendar");
                                fire.child(firebaseID).setValue(firebaseData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        System.out.println("Event updated to database");
                                    }
                                }) .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        System.out.println("Fail to update event in database");
                                    }
                                });
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
    }

    // Delete week event from Firebase with the existing data
    public void deleteWeekEventFromFirebase (final long calendarID) {
        // Getting all the firebase data again to do the check and delete using firebase id accordingly
        DatabaseReference firebase = FirebaseDatabase.getInstance().getReference().child(NavigationBar.acctName).child("Calbitica").child("Calendar");
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get the unique key for each, due to cannot pin-point the unique value
                    Map<String, String> eventData = (Map<String, String>) dataSnapshot.getValue();
                    for (String key : eventData.keySet()) {
                        Object data = eventData.get(key);
                        try {
                            // Retrieve as a object, easier to do the conversion
                            HashMap<String, Object> eventObject = (HashMap<String, Object>) data;

                            // Making use of calendarID to do the checking(Unique id also)
                            long eventID = (long) eventObject.get("calendarID");

                            // Check and render the firebase with the existing data(Only 1 data will be found)
                            if (eventID == calendarID) {
                                // firebaseID will be the parent of the key, which needed this to delete
                                String firebaseID = eventData.getOrDefault(eventData.keySet(), key);

                                // Deleting by using the parent key, to remove the whole of the child values
                                DatabaseReference fire = FirebaseDatabase.getInstance().getReference().child(NavigationBar.acctName).child("Calbitica").child("Calendar");
                                fire.child(firebaseID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        System.out.println("Event successfully removed from database");
                                    }
                                }) .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        System.out.println("Fail to delete event from database");
                                    }
                                });
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
    }

    // Get the schedule events from firebase, only render once(will be discard after that)
    public void getScheduleEventsFromFirebase(final List<CalendarEvent> eventList) {
        // Get the events from firebase, only render once(will be discard after that)
        /*
        DatabaseReference firebase = FirebaseDatabase.getInstance().getReference().child(NavigationBar.acctName).child("Calbitica").child("Calendar");
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get the unique key for each, due to cannot pin-point the unique value
                    Map<String, String> eventData = (Map<String, String>) dataSnapshot.getValue();
                    for (String key : eventData.keySet()) {
                        Object data = eventData.get(key);

                        try {
                            // Retrieve as a object, easier to do the conversion
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

                            // Getting the JSON Object from colorInfo, based on key
                            HashMap<String, Object> colorObject = (HashMap<String, Object>) eventObject.get("colorInfo");
                            String colorText = colorObject.get("color").toString();
                            int color = Integer.parseInt(colorText);

                            // Based on the Schedule Calendar format, and return back the list
                            BaseCalendarEvent allEvent = new BaseCalendarEvent(title, "", "", color, startDateTime, endDateTime, false);
                            allEvent.setId(calendarID);
                            eventList.add(allEvent);
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

                    System.out.println("Events successfully retrieved from database");
                } else {
                    System.out.println("Fail to retrieve from database");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println(databaseError.getCode());
            }
        });

         */
    }

    // For the Schedule Calendar (Create, Edit, Delete) will be the same as the weekView Calendar...
}
