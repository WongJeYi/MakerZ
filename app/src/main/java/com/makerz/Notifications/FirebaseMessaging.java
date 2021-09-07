package com.makerz.Notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.makerz.PrivateGroupChatActivity;
import com.makerz.R;
import com.makerz.model.Message;

public class FirebaseMessaging extends FirebaseMessagingService{//com.google.firebase.messaging.FirebaseMessagingService {

    /*public FirebaseMessaging() {
    }*/

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Get current user from shared preferences
        SharedPreferences sp = getSharedPreferences("SP_GROUPNAME", MODE_PRIVATE);
        String savedChatRoom = sp.getString("Current_GROUPNAME", "None");

        //Message message = new Message();
        String sent = remoteMessage.getData().get("from");
     // String sent = message.getFrom();

        // !!!!!!Might need to change this one after understanding what !savedCurrentUser.equals(user) does
        String chatRoomName = remoteMessage.getData().get("to");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && sent.equals(firebaseUser.getUid()))
        {
            if(!savedChatRoom.equals(chatRoomName))
            {
                Toast.makeText(FirebaseMessaging.this,"Hello2",  Toast.LENGTH_SHORT).show();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                {
                  sendOAndAboveNotification(remoteMessage);
                }
                else
                    {
                        sendNormalNotification(remoteMessage);
                    }
            }
        }
    }

    private void sendNormalNotification (RemoteMessage remoteMessage)
    {
        Toast.makeText(FirebaseMessaging.this,"Hello",  Toast.LENGTH_SHORT).show();
        String chatRoomName = remoteMessage.getData().get("to");
        //  String title = remoteMessage.getData().get("to");
        String body = remoteMessage.getData().get("message");


        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(chatRoomName.replaceAll("[\\D]","")); // not sure if this is neccessary
        Intent intent = new Intent(this, PrivateGroupChatActivity.class);
        Bundle bundle = new Bundle();
        // In the video, it is used for hisUid = intent.getStringExtra("hisUid"); in PrivateGroupChatActivity
        // And here it is bundle.putString("hisUid",user);  , where user = chatRoomName in our case
        bundle.putString("groupName",chatRoomName);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                // .setSmallIcon(Integer.parseInt(icon))
                .setSmallIcon(R.drawable.makerz)
                .setContentText(body)
                .setContentTitle(chatRoomName) // may have error
                .setAutoCancel(true)
                .setSound(defSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int j = 0;
        if (i>0)
        {
            j = 1;
        }
        notificationManager.notify(j,builder.build());
    }

    private void sendOAndAboveNotification (RemoteMessage remoteMessage)
    {
        Toast.makeText(FirebaseMessaging.this,"Hello1",  Toast.LENGTH_SHORT).show();
        String chatRoomName = remoteMessage.getData().get("to");
        //  String title = remoteMessage.getData().get("to");
        String body = remoteMessage.getData().get("message");

      //  String icon = getURLForResource(R.drawable-xxhdpi.makerz);


        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(chatRoomName.replaceAll("[\\D]",""));
        Intent intent = new Intent(this, PrivateGroupChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("groupName",chatRoomName);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        OreoAndAboveNotification notification1 = new OreoAndAboveNotification(this);
        Notification.Builder builder = notification1.getONotifications(chatRoomName, body, pendingIntent, defSoundUri);

        int j = 0;
        if (i>0)
        {
            j = 1;
        }
        notification1.getManager().notify(j,builder.build());
    }

    public String getURLForResource (int resourceId)
    {
        return Uri.parse("android.resource://" + R.class.getPackage().getName() + "/" + resourceId).toString();
    }

}
