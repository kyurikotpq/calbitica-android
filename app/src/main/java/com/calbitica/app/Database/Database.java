package com.calbitica.app.Database;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.Toast;

import com.alamkanak.weekview.WeekViewEvent;
import com.arasthel.asyncjob.AsyncJob;
import com.calbitica.app.Models.Calbit.Calbit;
import com.calbitica.app.Models.Calbit.Calbits;
import com.calbitica.app.Models.Calbit.StartDateTime;
import com.calbitica.app.Models.Calendars.Calendar;
import com.calbitica.app.Models.Calendars.Calendars;
import com.calbitica.app.Util.CalbiticaAPI;
import com.calbitica.app.Util.UserData;

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
    List<Calendar> allCalendarInfo;
    List<Calbit> allCalbitInfo;

    public Database(Context context) {mcontext = context;}

    public List<Calendar> GetAllCalendars() {
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

    public List<Calbit> GetAllCalbit() {
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
