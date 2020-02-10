package com.calbitica.app.Notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.calbitica.app.NavigationBar.NavigationBar;
import com.calbitica.app.R;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static com.calbitica.app.NavigationBar.NavigationBar.eventEnd;
import static com.calbitica.app.NavigationBar.NavigationBar.eventName;
import static com.calbitica.app.NavigationBar.NavigationBar.eventStart;

public class Notification extends BroadcastReceiver {
    // Live Notification upon app open
    @Override
    public void onReceive(Context context, Intent intent) {
        String eventDateTime = "";

        if(eventStart != null && eventEnd != null) {
            eventDateTime = eventStart + " - " + eventEnd;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Calbitica")
                .setSmallIcon(R.drawable.favicon)
                .setContentTitle(eventName)
                .setContentText(eventDateTime)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(eventDateTime) // expandable notification
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(200, builder.build());
    }
}