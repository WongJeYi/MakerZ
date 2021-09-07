package com.makerz.Notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.makerz.ChatActivity;
import com.makerz.PrivateGroupChatActivity;
import com.makerz.R;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessaging extends com.google.firebase.messaging.FirebaseMessagingService{

    public static int NOTIFICATION_ID = 1;
    static ArrayList<Map<String,String>> MESSAGES = new ArrayList<>();
    private DatabaseReference myRef;
    private String userId;
    private FirebaseAuth firebaseAuth;
    private String codename;
    private String Image;
    private static Intent intent;
    private String channel_id;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Map<String,String> data = remoteMessage.getData();
        String from=remoteMessage.getFrom();
        generateNotification(from,data,remoteMessage.getNotification().getBody(),remoteMessage.getNotification().getTitle());
        Log.e("Notification",remoteMessage.getFrom()+remoteMessage.getNotification().getBody()+remoteMessage.getNotification().getTitle());
    }

    private void generateNotification(String from,final Map<String,String> data,String body, String title) {
        if(from.equals("/topics/"+getString(R.string.main_chat)+"MakerZ")){
            intent = new Intent(MyFirebaseMessaging.this, ChatActivity.class);
            channel_id="channel2";
        }else{
            intent = new Intent(MyFirebaseMessaging.this, PrivateGroupChatActivity.class);
            intent.putExtra("groupName", data.get("Receiver"));
            channel_id="channel1";
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        firebaseAuth=FirebaseAuth.getInstance();
        userId=firebaseAuth.getCurrentUser().getUid();
        codename=data.get("name");
        Image="default_image";
        intent.putExtra("visit_user_id", userId);
        intent.putExtra("visit_user_name", codename);
        intent.putExtra("visit_image",Image );
        PendingIntent pendingIntent = PendingIntent.getActivity(MyFirebaseMessaging.this,0,intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.MessagingStyle messagingStyle =
                new NotificationCompat.MessagingStyle("");
        messagingStyle.setConversationTitle("Group Message • "+title);
        MESSAGES.add(data);
        for (Map<String,String> chatMessage : MESSAGES) {
            if(chatMessage.get("Receiver").equals(title)) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyyhh:mm a");
                Date date = new Date();
                try {
                    date = simpleDateFormat.parse(chatMessage.get("date").concat(chatMessage.get("time")));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Timestamp ts = new Timestamp(date.getTime());
                String message = chatMessage.get("message");

                NotificationCompat.MessagingStyle.Message notificationMessage =
                        new NotificationCompat.MessagingStyle.Message(
                                message,
                                ts.getTime(),
                                chatMessage.get("from")
                        );
                messagingStyle.addMessage(notificationMessage);
            }
        }
        NotificationCompat.Builder notificationBuilder;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notificationBuilder = new NotificationCompat.Builder(MyFirebaseMessaging.this,channel_id)
                    .setSmallIcon(R.drawable.makerz)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setStyle(messagingStyle)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setAutoCancel(true)
                    .setSound(soundUri)
                    .setOnlyAlertOnce(true)
                    .setContentIntent(pendingIntent);
        }else {
            notificationBuilder = new NotificationCompat.Builder(MyFirebaseMessaging.this)
                    .setSmallIcon(R.drawable.makerz)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(body))
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setSound(soundUri)
                    .setOnlyAlertOnce(true)
                    .setContentIntent(pendingIntent);
        }
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        if(NOTIFICATION_ID > 1073741824)
        {
            NOTIFICATION_ID = 0;
        }


        notificationManager.notify(stringToBigInteger(title).intValue(), notificationBuilder.build());


    }
    public BigInteger stringToBigInteger(String text) {
        BigInteger bigInt = new BigInteger(text.getBytes());
        return bigInt;
    }
}
