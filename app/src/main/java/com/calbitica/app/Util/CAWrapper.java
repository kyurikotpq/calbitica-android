package com.calbitica.app.Util;

import android.content.Context;
import android.widget.Toast;

import com.arasthel.asyncjob.AsyncJob;
import com.calbitica.app.Models.Calbit.Calbit;
import com.calbitica.app.Models.Calbit.Calbits;
import com.calbitica.app.Models.Calendars.CalbiticaCalendar;
import com.calbitica.app.Models.Calendars.Calendars;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Wrapper for shared functionalities
// of the Calbitica API
// C & U are handled by WeekSaveEvent, so there's no need
// to put them here

public class CAWrapper {
    static List<CalbiticaCalendar> allCalbiticaCalendars;
    static List<Calbit> allCalbitInfo;

    // Get all calendars
    public static void getAllCalendars(Context mcontext, CalListResultInterface listenerClass) {
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
                        listenerClass.onCalendarListResult(allCalbiticaCalendars);
                    }
                });
            }

            @Override
            public void onFailure(Call<Calendars> call, Throwable t) {
                System.out.println("Fail to get all Calendars: " + t.getMessage());
            }
        });
    }

    // Get all calbits
    public static void getAllCalbits(Context mcontext, CalbitResultInterface listenerClass) {
        // Retrieve the JWT
        String jwt = UserData.get("jwt", mcontext);

        // Build the API Call
        Call<Calbits> apiCall = CalbiticaAPI.getInstance(jwt).calbit().getAllCalbits();

        // Make the API Call in BG thread
        apiCall.enqueue(new Callback<Calbits>() {
            @Override
            public void onResponse(Call<Calbits> call, Response<Calbits> response) {
                if (!response.isSuccessful() && response.code() == 410) {
                    Toast.makeText(mcontext, "You may have revoked Calbitica's access to your account."
                                    + " Please sign in again.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                Calbits allCalbits = response.body();

                if (allCalbits != null) {
                    // Get the fresh list of calbits
                    allCalbitInfo = allCalbits.getData();

                    AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                        @Override
                        public void doInUIThread() {
                            listenerClass.onCalbitListResult(allCalbitInfo);
                        }
                    });

                } else {
                    Toast.makeText(mcontext, "You don't have any events yet.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Calbits> call, Throwable t) {
                System.out.println("Fail to get all calbits " + t.getMessage());
            }
        });
    }

    // Update Calbit completion status
    public static void updateCalbitCompletion(Context mcontext, String _id,
                                              HashMap<String, Boolean> completion,
                                              CalbitResultInterface listenerClass) {
        // Retrieve the JWT
        String jwt = UserData.get("jwt", mcontext);

        // Build the API Call
        Call<HashMap<String, Object>> apiCall = CalbiticaAPI.getInstance(jwt).calbit()
                .updateCalbitStatus(_id, completion);

        // Make the API Call
        apiCall.enqueue(new Callback<HashMap<String, Object>>() {
            @Override
            public void onResponse(Call<HashMap<String, Object>> call, Response<HashMap<String, Object>> response) {
                if (!response.isSuccessful()) {
                    System.out.println("Unsuccessful to update calbit completion " + response.code());
                    return;
                }

                // No news is good news
            }

            @Override
            public void onFailure(Call<HashMap<String, Object>> call, Throwable t) {
                System.out.println("Fail to get update completion of calbit " + t.getMessage());

                AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                    @Override
                    public void doInUIThread() {
                        listenerClass.onCalbitCompletionFailure();
                    }
                });
            }
        });
    }

    public static void deleteCalbit(Context mcontext, String _id,
                                    CalbitResultInterface listenerClass) {
        // Retrieve the JWT
        String jwt = UserData.get("jwt", mcontext);

        // Build the API Call
        Call<Void> apiCall = CalbiticaAPI.getInstance(jwt).calbit().deleteCalbit(_id);

        // Make the API Call
        apiCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    try {
                        System.out.println("Unsuccessful to delete event in calbits " + response.errorBody().string());
                        return;
                    } catch(Exception e) { }
                }
                System.out.println("Calbitica Database deleted is: " + response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                System.out.println("Fail to delete in calbits " + t.getMessage());
            }
        });
    }

}
