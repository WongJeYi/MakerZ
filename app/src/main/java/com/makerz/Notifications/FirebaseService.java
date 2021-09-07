package com.makerz.Notifications;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseService extends FirebaseMessagingService{//MyFirebaseMessaging {

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
      //  String tokenRefresh = FirebaseInstanceId.getInstance().getInstanceId().getResult().getToken();

        FirebaseInstallations.getInstance().getToken(true).addOnCompleteListener(new OnCompleteListener<InstallationTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<InstallationTokenResult> task) {
                if (user != null)
                {
                    String tokenRefresh =  task.getResult().getToken();
                    updateToken(tokenRefresh);
                }
            }


        });

       /* if (user != null)
        {
            updateToken(tokenRefresh);
        }*/
    }

    private void updateToken(String tokenRefresh) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
        final Token token = new Token(tokenRefresh);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {
                    String fullName = dataSnapshot.child("fullname").getValue().toString();
                    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Tokens");
                    myRef.child(fullName).setValue(token);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

}
