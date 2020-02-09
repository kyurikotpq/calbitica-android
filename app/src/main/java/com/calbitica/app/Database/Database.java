package com.calbitica.app.Database;

import android.content.Context;
import android.graphics.Color;
import android.widget.CheckBox;
import android.widget.Toast;

import com.alamkanak.weekview.WeekViewEvent;
import com.arasthel.asyncjob.AsyncJob;
import com.calbitica.app.Models.Calbit.Calbit;
import com.calbitica.app.Models.Calbit.Calbits;
import com.calbitica.app.Models.Calbit.StartDateTime;
import com.calbitica.app.Models.Calendars.CalbiticaCalendar;
import com.calbitica.app.Models.Calendars.Calendars;
import com.calbitica.app.R;
import com.calbitica.app.Util.CalbiticaAPI;
import com.calbitica.app.Util.UserData;
import com.github.tibolte.agendacalendarview.models.BaseCalendarEvent;
import com.github.tibolte.agendacalendarview.models.CalendarEvent;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.calbitica.app.Week.WeekFragment.listOfCalbits;
import static com.calbitica.app.Week.WeekFragment.mNewEvents;
import static com.calbitica.app.Week.WeekFragment.weekView;

public class Database {
    Context mcontext;
    List<CalbiticaCalendar> allCalendarInfo;
    List<Calbit> allCalbitInfo;

    public Database(Context context) {mcontext = context;}

    public List<CalbiticaCalendar> getAllCalendars() {
        new AsyncJob.AsyncJobBuilder<Boolean>().doInBackground(new AsyncJob.AsyncAction<Boolean>() {
            @Override
            public Boolean doAsync() {
                // Retrieve the JWT
                String jwt = UserData.get("jwt", mcontext);

                // Build the API Call
                Call<Calendars> apiCall = CalbiticaAPI.getInstance(jwt).calendars().getAllCalendars();

                // Make the API Call
                apiCall.enqueue(new Callback<Calendars>() {
                    @Override
                    public void onResponse(Call<Calendars> call, Response<Calendars> response) {
                        if (!response.isSuccessful()) {
                            System.out.println("Unsuccessful to get all Calendars: " + response.code());
                            return;
                        }

                        // Due to the allCalendars parent data require an array with a child method, so have to use loop here...
                        Calendars allCalendars = response.body();
                        allCalendarInfo = allCalendars.getData();
                    }

                    @Override
                    public void onFailure(Call<Calendars> call, Throwable t) {
                        System.out.println("Fail to get all Calendars: " + t.getMessage());
                    }
                });

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }).create().start();

        return allCalendarInfo;
    }

    public void getAllCalbitAndRenderOnWeek() {
        new AsyncJob.AsyncJobBuilder<Boolean>().doInBackground(new AsyncJob.AsyncAction<Boolean>() {
            @Override
            public Boolean doAsync() {
                // Retrieve the JWT
                String jwt = UserData.get("jwt", mcontext);

                // Build the API Call
                Call<Calbits> apiCall = CalbiticaAPI.getInstance(jwt).calbit().getAllCalbits();

                // Make the API Call
                apiCall.enqueue(new Callback<Calbits>() {
                    @Override
                    public void onResponse(Call<Calbits> call, Response<Calbits> response) {
                        if (!response.isSuccessful()) {
                            System.out.println("Unsuccessful to get all calbits " + response.code());
                            return;
                        }

                        Calbits allCalbits = response.body();

                        if(allCalbits.getData() != null) {
                            // Get the fresh list of calbits
                            listOfCalbits = allCalbits.getData();

                            // save each calbit as a new WeekViewEvent
                            for(int i = 0; i < listOfCalbits.size(); i++) {
                                Calbit currentCalbit = listOfCalbits.get(i);

                                // Declare the necessary fields into Week View Calendar
                                java.util.Calendar startDateTime = java.util.Calendar.getInstance();
                                java.util.Calendar endDateTime = java.util.Calendar.getInstance();

                                if(currentCalbit.getStart().getDate() != null) {
                                    try {
                                        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                                        startDateTime.setTime(sdf.parse(currentCalbit.getStart().getDate().toString()));
                                    } catch (java.text.ParseException e) {
                                        e.printStackTrace();
                                    }
                                } else if (currentCalbit.getStart().getDateTime() != null) {
                                    try {
                                        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                                        startDateTime.setTime(sdf.parse(currentCalbit.getStart().getDateTime().toString()));
                                    } catch (java.text.ParseException e) {
                                        e.printStackTrace();
                                    }
                                }

                                if(currentCalbit.getEnd().getDate() != null) {
                                    try {
                                        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                                        endDateTime.setTime(sdf.parse(currentCalbit.getEnd().getDate().toString()));
                                    } catch (java.text.ParseException e) {
                                        e.printStackTrace();
                                    }

                                } else if (currentCalbit.getEnd().getDateTime() != null) {
                                    try {
                                        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                                        endDateTime.setTime(sdf.parse(currentCalbit.getEnd().getDateTime().toString()));
                                    } catch (java.text.ParseException e) {
                                        e.printStackTrace();
                                    }
                                }

                                // Render all the data into WeekView Calendar
                                WeekViewEvent weekEvents = new WeekViewEvent (i, currentCalbit.getSummary(), startDateTime, endDateTime);
                                weekEvents.setAllDay(currentCalbit.getAllDay());

                                // Auto-configure the task completion of color and checked according to calbitica
                                // Can't do the checkbox here accordingly, due to this functions should not indirect any
                                // of the components in the WeekFragment, have to check separately
                                if(currentCalbit.getCompleted() != null) {
                                    if(currentCalbit.getCompleted().getStatus()) {
                                        weekEvents.setColor(Color.rgb(200, 200, 200));
                                    } else {
                                        weekEvents.setColor(Color.rgb(100, 200, 220));
                                    }
                                }

                                mNewEvents.add(weekEvents);

                                // Refresh the Week Calendar
                                weekView.notifyDatasetChanged();
                            }
                        } else {
                            Toast.makeText(mcontext, "There is no data found in database", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Calbits> call, Throwable t) {
                        System.out.println("Fail to get all calbits " + t.getMessage());
                    }
                });

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }).create().start();
    }

    public void getAllCalbitAndRenderOnAgenda(final List<CalendarEvent> eventList) {
        new AsyncJob.AsyncJobBuilder<Boolean>().doInBackground(new AsyncJob.AsyncAction<Boolean>() {
            @Override
            public Boolean doAsync() {
                // Retrieve the JWT
                String jwt = UserData.get("jwt", mcontext);

                // Build the API Call
                Call<Calbits> apiCall = CalbiticaAPI.getInstance(jwt).calbit().getAllCalbits();

                // Make the API Call
                apiCall.enqueue(new Callback<Calbits>() {
                    @Override
                    public void onResponse(Call<Calbits> call, Response<Calbits> response) {
                        if (!response.isSuccessful()) {
                            System.out.println("Unsuccessful to get all calbits " + response.code());
                            return;
                        }

                        Calbits allCalbits = response.body();

                        if(allCalbits.getData() != null) {
                            // Get the fresh list of calbits
                            listOfCalbits = allCalbits.getData();

                            // save each calbit as a new WeekViewEvent
                            for(int i = 0; i < listOfCalbits.size(); i++) {
                                Calbit currentCalbit = listOfCalbits.get(i);

                                // Declare the necessary fields into Week View Calendar
                                java.util.Calendar startDateTime = java.util.Calendar.getInstance();
                                java.util.Calendar endDateTime = java.util.Calendar.getInstance();

                                if(currentCalbit.getStart().getDate() != null) {
                                    try {
                                        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                                        startDateTime.setTime(sdf.parse(currentCalbit.getStart().getDate().toString()));
                                    } catch (java.text.ParseException e) {
                                        e.printStackTrace();
                                    }
                                } else if (currentCalbit.getStart().getDateTime() != null) {
                                    try {
                                        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                                        startDateTime.setTime(sdf.parse(currentCalbit.getStart().getDateTime().toString()));
                                    } catch (java.text.ParseException e) {
                                        e.printStackTrace();
                                    }
                                }

                                if(currentCalbit.getEnd().getDate() != null) {
                                    try {
                                        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                                        endDateTime.setTime(sdf.parse(currentCalbit.getEnd().getDate().toString()));
                                    } catch (java.text.ParseException e) {
                                        e.printStackTrace();
                                    }

                                } else if (currentCalbit.getEnd().getDateTime() != null) {
                                    try {
                                        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                                        endDateTime.setTime(sdf.parse(currentCalbit.getEnd().getDateTime().toString()));
                                    } catch (java.text.ParseException e) {
                                        e.printStackTrace();
                                    }
                                }

                                // Based on the Agenda Calendar format, and return back the list
                                BaseCalendarEvent allEvent = new BaseCalendarEvent(currentCalbit.getSummary(), "", "", 0, startDateTime, endDateTime, false);

                                // Auto-configure the task completion of color and checked according to calbitica
                                // Can't do the checkbox here accordingly, due to this functions should not indirect any
                                // of the components in the WeekFragment, have to check separately
                                // Overwrite the previous color, due to required fields from libraries
                                if(currentCalbit.getCompleted() != null) {
                                    if(currentCalbit.getCompleted().getStatus()) {
                                        allEvent.setColor(Color.rgb(200, 200, 200));
                                    } else {
                                        allEvent.setColor(Color.rgb(100, 200, 220));
                                    }
                                }
                                allEvent.setId(i);

                                eventList.add(allEvent);
                            }
                        } else {
                            Toast.makeText(mcontext, "There is no data found in database", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Calbits> call, Throwable t) {
                        System.out.println("Fail to get all calbits " + t.getMessage());
                    }
                });

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }).create().start();
    }

    public List<Calbit> getAllCalbit() {
        new AsyncJob.AsyncJobBuilder<Boolean>().doInBackground(new AsyncJob.AsyncAction<Boolean>() {
            @Override
            public Boolean doAsync() {
                // Retrieve the JWT
                String jwt = UserData.get("jwt", mcontext);

                // Build the API Call
                Call<Calbits> apiCall = CalbiticaAPI.getInstance(jwt).calbit().getAllCalbits();

                // Make the API Call
                apiCall.enqueue(new Callback<Calbits>() {
                    @Override
                    public void onResponse(Call<Calbits> call, Response<Calbits> response) {
                        if (!response.isSuccessful()) {
                            System.out.println("Unsuccessful to get all calbits " + response.code());
                            return;
                        }

                        Calbits allCalbits = response.body();
                        allCalbitInfo = allCalbits.getData();
                    }

                    @Override
                    public void onFailure(Call<Calbits> call, Throwable t) {
                        System.out.println("Fail to get all calbits " + t.getMessage());
                    }
                });

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }).create().start();

        return allCalbitInfo;
    }

    public void updateEventInCalbit(final String _id, final String summary, final Date start, final Date end, final Date reminders, String calendarID, String googleID, Boolean allDay) {
        new AsyncJob.AsyncJobBuilder<Boolean>().doInBackground(new AsyncJob.AsyncAction<Boolean>() {
            @Override
            public Boolean doAsync() {
//                // Retrieve the JWT
//                String jwt = UserData.get("jwt", mcontext);
//
//                StartDateTime startDateTime = new StartDateTime();
//                startDateTime.set();
//
//                Calbit putBody = new Calbit(_id, summary, start, end, reminders, calendarID, googleID, allDay);
//
//                // Build the API Call
//                Call<Calbit> apiCall = CalbiticaAPI.getInstance(jwt).calbit().editCalbit(_id, putBody);
//
//                // Make the API Call
//                apiCall.enqueue(new Callback<Calbit>() {
//                    @Override
//                    public void onResponse(Call<Calbit> call, Response<Calbit> response) {
//                        if (!response.isSuccessful()) {
//                            System.out.println("Unsuccessful to update event in calbits " + response.code());
//                            return;
//                        }
//
//                        Calbit allCalbits = response.body();
//                    }
//
//                    @Override
//                    public void onFailure(Call<Calbit> call, Throwable t) {
//                        System.out.println("Fail to update event in calbits " + t.getMessage());
//                    }
//                });

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }).create().start();
    }

    public void deleteEventInCalbit(final String _id) {
        new AsyncJob.AsyncJobBuilder<Boolean>()
        .doInBackground(new AsyncJob.AsyncAction<Boolean>() {
            @Override
            public Boolean doAsync() {
                // Retrieve the JWT
                String jwt = UserData.get("jwt", mcontext);

                // Build the API Call
                Call<Void> apiCall = CalbiticaAPI.getInstance(jwt).calbit().deleteCalbit(_id);

                // Make the API Call
                apiCall.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (!response.isSuccessful()) {
                            System.out.println("Unsuccessful to delete event in calbits " + response.code());
                            return;
                        }

                        System.out.println("Calbitica Database deleted is: " + response.isSuccessful());
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        System.out.println("Fail to delete in calbits " + t.getMessage());
                    }
                });

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }).create().start();
    }

    /*
    public void updateEventStatusInCalbit(final String _id, final Boolean status) {
        new AsyncJob.AsyncJobBuilder<Boolean>()
        .doInBackground(new AsyncJob.AsyncAction<Boolean>() {
            @Override
            public Boolean doAsync() {
                // Retrieve the JWT
                String jwt = UserData.get("jwt", mcontext);

                // Saved the Changes to the task status
                Calbit mongoStatus = new Calbit(status);

                // Build the API Call
                Call<Calbit> apiCall = CalbiticaAPI.getInstance(jwt).calbit().updateCalbitStatus(_id, mongoStatus);

                // Make the API Call
                apiCall.enqueue(new Callback<Calbit>() {
                    @Override
                    public void onResponse(Call<Calbit> call, Response<Calbit> response) {
                        if (!response.isSuccessful()) {
                            System.out.println("Unsuccessful to update event status in calbits " + response.raw());
                            return;
                        }

                        System.out.println("Calbitica successfully update status: " + response.isSuccessful());
                    }

                    @Override
                    public void onFailure(Call<Calbit> call, Throwable t) {
                        System.out.println("Fail tto update event status in calbits " + t.getMessage());
                    }
                });

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }).create().start();
    }
    */
}
