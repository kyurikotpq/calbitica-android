package com.calbitica.app.Notification;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.calbitica.app.Models.Calbit.EndDateTime;
import com.calbitica.app.Models.Calbit.StartDateTime;
import com.calbitica.app.NavigationBar.NavigationBar;
import com.calbitica.app.R;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.calbitica.app.NavigationBar.NavigationBar.eventEnd;
import static com.calbitica.app.NavigationBar.NavigationBar.eventName;
import static com.calbitica.app.NavigationBar.NavigationBar.eventStart;

public class Notification extends BroadcastReceiver {
    // Live Notification upon app open
    @Override
    public void onReceive(Context context, Intent intent) {
        String eventDateTime = "";

        if(eventStart.getDate() != null && eventEnd.getDate() != null) {
            eventDateTime = eventStart.getDate() + " - " + eventEnd.getDate();
        } else if (eventStart.getDateTime() != null && eventEnd.getDateTime() != null){
            eventDateTime = eventStart.getDateTime() + " - " + eventEnd.getDateTime();
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Calbitica")
                .setSmallIcon(R.drawable.favicon)
                .setContentTitle(NavigationBar.acctName)
                .setContentText("Today Event: " + eventName + "\n" + eventDateTime)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(200, builder.build());
    }
}
