package com.calbitica.app.Notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import com.calbitica.app.NavigationBar.NavigationBar;
import com.calbitica.app.R;
import androidx.core.app.NotificationCompat;

// Due to Manifest only allow 1 application element only, merge with the Internet.CheckInternetConnection
public class Notification extends com.calbitica.app.Internet.CheckInternetConnection {
    public static final String CHANNEL_ID = "Calbitica";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannels();
    }

    // Check the SDK_INT version to run only on Android 8.0 (API level 26) and higher,
    // Because the notification channels APIs are not available in the support library
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_ID,
                NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }
    }

    // Populate the notification here
    public static void getNotification() {
        android.app.Notification notification = new NotificationCompat.Builder(getInstance(), CHANNEL_ID)
                .setSmallIcon(R.drawable.favicon)
                .setContentTitle(NavigationBar.acctName)
                .setContentText("Today, You have " + NavigationBar.eventSize.size() + " events")
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();

        NavigationBar.notificationManager.notify(1, notification);
    }
}
