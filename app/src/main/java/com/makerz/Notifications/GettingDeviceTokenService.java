package com.makerz.Notifications;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.installations.FirebaseInstallations;

public class GettingDeviceTokenService extends com.google.firebase.messaging.FirebaseMessagingService{


    @Override
    public void onNewToken(@NonNull String DeviceToken) {
        DeviceToken = FirebaseInstallations.getInstance().getToken(true).getResult().getToken();
        Log.e("DEVICE TOKEN", DeviceToken);
    }

}
