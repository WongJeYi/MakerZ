package com.makerz.Notifications;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {

    public static final String CHANNEL_1_ID ="channel1";
    public static final String CHANNEL_2_ID ="channel2";

    @Override
    public void onCreate()
    {
        super.onCreate();
        createNotificationChannels();
    }

    private void createNotificationChannels()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Small Group Chat",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel1.setDescription("You can listen to newest group chat updates.");

            NotificationChannel channel2 = new NotificationChannel(
                    CHANNEL_2_ID,
                    "Main Chat",
                    NotificationManager.IMPORTANCE_LOW
            );

            channel2.setDescription("You can listen to newest public chat updates.");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
            manager.createNotificationChannel(channel2);
        }
    }
}
