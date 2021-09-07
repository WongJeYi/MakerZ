package com.makerz.Notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.makerz.PrivateGroupChatActivity;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String currentGroupName = intent.getExtras().get("groupName").toString();
        String userId = intent.getExtras().get("visit_user_id").toString();
        String codename = intent.getExtras().get("visit_user_name").toString();
        String messageReceiverImage = intent.getExtras().get("visit_image").toString();


        Intent groupChatIntent = new Intent(context.getApplicationContext(), PrivateGroupChatActivity.class);
        groupChatIntent.putExtra("groupName",currentGroupName);
        groupChatIntent.putExtra("visit_user_id", userId);
        groupChatIntent.putExtra("visit_user_name", codename);
        groupChatIntent.putExtra("visit_image", messageReceiverImage);
        groupChatIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(groupChatIntent);
    }
}
