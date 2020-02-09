package com.calbitica.app.Util;

import android.content.Context;
import android.widget.Toast;

import com.arasthel.asyncjob.AsyncJob;
import com.calbitica.app.Models.Calbit.Calbit;
import com.calbitica.app.Models.Calbit.Calbits;
import com.calbitica.app.Models.Calendars.CalbiticaCalendar;
import com.calbitica.app.Models.Calendars.Calendars;
import com.calbitica.app.Week.CalListResultInterface;
import com.calbitica.app.Week.WeekCreateEvent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Wrapper for some 'global' functionalities
// of the Calbitica API
public class CAWrapper {
    static List<CalbiticaCalendar> allCalbiticaCalendars;
    static List<Calbit> allCalbitInfo;
    static CalListResultInterface calListListener;

    public static void getAllCalendars(Context mcontext, CalListResultInterface listenerClass) {
        calListListener = listenerClass;
        String jwt = UserData.get("jwt", mcontext);

        // Build the API Call
        Call<Calendars> apiCall = CalbiticaAPI.getInstance(jwt).calendars().getAllCalendars();

        // Make the API Call in the background thread
        apiCall.enqueue(new Callback<Calendars>() {
            @Override
            public void onResponse(Call<Calendars> call, Response<Calendars> response) {
                if (!response.isSuccessful()) {
                    System.out.println("Unsuccessful to get all Calendars: " + response.code());
                    return;
                }

                // Due to the allCalendars parent data require an array with a child method, so have to use loop here...
                Calendars allCalendars = response.body();
                allCalbiticaCalendars = allCalendars.getData();

                AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                    @Override
                    public void doInUIThread() {
                        System.out.println("NOW IN ui THREAD");
                        calListListener.onCalendarListResult(allCalbiticaCalendars);
                    }
                });
                System.out.println("CALEDNARS SAVED");
                System.out.println(allCalbiticaCalendars);
            }

            @Override
            public void onFailure(Call<Calendars> call, Throwable t) {
                System.out.println("Fail to get all Calendars: " + t.getMessage());
            }
        });
    }

    /*
    public List<Calbit> getAllCalbit(Context mcontext) {
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
                        Calbits allCalbits = response.body();

                        if(allCalbits.getData() != null) {
                            // Get the fresh list of calbits
                            allCalbitInfo = allCalbits.getData();
                        } else {
                            Toast.makeText(mcontext, "There is no data found in CAWrapper", Toast.LENGTH_SHORT).show();
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
        }).doWhenFinished(new AsyncJob.AsyncResultAction<Boolean>() {
            @Override
            public void onResult(Boolean result) {
                AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                    @Override
                    public void doInUIThread() {

                        // save each calbit as a new WeekViewEvent
                        for(int i = 0; i < listOfCalbits.size(); i++) {
                            Calbit currentCalbit = listOfCalbits.get(i);

                            // Declare the necessary fields into Week View CalbiticaCalendar
                            Calendar startDateTime = Calendar.getInstance();
                            Calendar endDateTime = Calendar.getInstance();

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

                            // Render all the data into WeekView CalbiticaCalendar
                            WeekViewEvent weekEvents = new WeekViewEvent (i, currentCalbit.getSummary(), startDateTime, endDateTime);
                            weekEvents.setAllDay(currentCalbit.getAllDay());
                            mNewEvents.add(weekEvents);

                        }

                        // Refresh the Week CalbiticaCalendar
                        weekView.notifyDatasetChanged();

                        System.out.println("NOW IN ui THREAD");

                        if (mcontext instanceof WeekCreateEvent) {
                            ((WeekCreateEvent) mcontext).updateCalendarSpinner(allCalbiticaCalendars);

                        }
                    }
                });
            }
        }
        ).create().start();

        return allCalbitInfo;
    }
*/
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
//                            System.out.println("Unsuccessful to get all calbits " + response.code());
//                            return;
//                        }
//
//                        Calbit allCalbits = response.body();
//                    }
//
//                    @Override
//                    public void onFailure(Call<Calbit> call, Throwable t) {
//                        System.out.println("Fail to get all calbits " + t.getMessage());
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
}
