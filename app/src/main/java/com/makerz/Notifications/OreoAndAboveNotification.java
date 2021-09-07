package com.makerz.Notifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import com.makerz.R;

public class OreoAndAboveNotification extends ContextWrapper {

    public static final String ID = "some_id";
    public static final String NAME = "FirebaseAPP";

    private NotificationManager notificationManager;

    public OreoAndAboveNotification(Context base) {
        super(base);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
         createChannel();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel notificationChannel = new NotificationChannel(ID, NAME, NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(notificationChannel);

    }

    public  NotificationManager getManager(){
       if(notificationManager == null)
       {
           notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
       }
        return notificationManager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder getONotifications(String to,//title,
                                                  String message,
                                                  PendingIntent pendingIntent,
                                                  Uri soundUri)
                                                  //,String icon)
    {

      return new Notification.Builder(getApplicationContext(), ID)
              .setContentIntent(pendingIntent)
              .setContentTitle(to) //.setContentTitle(title)
              .setContentText(message)
              .setSound(soundUri)
              .setAutoCancel(true)
              .setSmallIcon(R.drawable.logo);
        // .setSmallIcon(Integer.parseInt(icon))
    }


}
