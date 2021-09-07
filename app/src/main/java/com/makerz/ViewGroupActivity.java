package com.makerz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.makerz.Notifications.APIService;
import com.makerz.adapter.CustomAdapter;

import java.util.ArrayList;

// This activity is used to display name of the groups which the user is a member/owner of.

public class ViewGroupActivity extends AppCompatActivity {

    ArrayList<String> groupChatNames = new ArrayList<>();
    ArrayList<String> alluserFullname = new ArrayList<>();
    private String userId, fullname, codename;
    private String[] retImage = {"default_image"};

    private DatabaseReference databaseReference,userReference,subscriptionReference;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference myRef;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser currentUser;

    private RecyclerView recyclerView;
    private String TAG ="View Group";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_group);

        // Clear array lists
        groupChatNames.clear();
        alluserFullname.clear();

        // Display back button on the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get current user's info.
        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser()!=null){

            currentUser = firebaseAuth.getCurrentUser();
            userId = firebaseAuth.getCurrentUser().getUid();

            firebaseDatabase = FirebaseDatabase.getInstance();
            myRef = FirebaseDatabase.getInstance().getReference();
            subscriptionReference = FirebaseDatabase.getInstance().getReference("Group Subscription");
            DatabaseReference reference = myRef.child("Users").child(userId);
            userReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    fullname = dataSnapshot.child("fullname").getValue().toString();
                    codename = dataSnapshot.child("codename").getValue().toString();

                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.hasChild("Images")) {
                            retImage[0] = dataSnapshot.child("Images").getValue().toString();
                        }
                    }

                    // Declaration of elements.
                    InitializeFields();
                    // Display groups.
                    RetrieveAndDisplayGroups();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    // Retrieve group names where the user is a member/owner of
    // and add to arrayList,
    // which will pass to CustomAdapter.
    private void RetrieveAndDisplayGroups() {

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    // If the current user is a member of the group, the group will be displayed.

                    if (ds.child("Group Members").child("Members").exists())
                    {
                        String groupMembers = ds.child("Group Members").child("Members").getValue().toString();

                        if (groupMembers.contains(fullname))
                        {
                            groupChatNames.add(ds.getKey());
                        }

                        // Retrieve group names where the user is a member/owner of and add to ArrayList groupChatNames,
                        // which will pass to CustomAdapter.
                        CustomAdapter adapter = new CustomAdapter(groupChatNames);
                        recyclerView.setAdapter(adapter);

                        if (getIntent() != null && getIntent().getExtras() != null) {
                            String currentGroupName = getIntent().getExtras().get("currentGroupName").toString();
                            Intent groupChatIntent = new Intent(ViewGroupActivity.this, PrivateGroupChatActivity.class);
                            groupChatIntent.putExtra("groupName", currentGroupName);
                            groupChatIntent.putExtra("visit_user_id", userId);
                            groupChatIntent.putExtra("visit_user_name", codename);
                            groupChatIntent.putExtra("visit_image", retImage[0]);

                            startActivity(groupChatIntent);
                            finish();

                        } else {
                            EnterPrivateGroup();
                        }
                    }
                }
                subscribeToNotification();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    // If either one of the groups which can be seen by user is pressed,
    // pass user's values and move to that specific group.
    private void EnterPrivateGroup() {
        recyclerView.addOnItemTouchListener(new MyRecycleViewClickListener(this, new MyRecycleViewClickListener.OnItemClickListener()
        {
            @Override
            public void onItemClick(View view, int position) {

                String currentGroupName = groupChatNames.get(position);
                Intent groupChatIntent = new Intent(ViewGroupActivity.this, PrivateGroupChatActivity.class);
                groupChatIntent.putExtra("groupName",currentGroupName);
                groupChatIntent.putExtra("visit_user_id", userId);
                groupChatIntent.putExtra("visit_user_name", codename);
                groupChatIntent.putExtra("visit_image", retImage[0]);

                startActivity(groupChatIntent);
                finish();
            }
        }));
    }

    // If back button on toolbar is pressed, pass user's values and move to ViewGroupMembers activity.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                Intent groupChatIntent = new Intent(ViewGroupActivity.this, ChatActivity.class);
                groupChatIntent.putExtra("groupName", "MakerZ");
                groupChatIntent.putExtra("visit_user_id",  userId);
                groupChatIntent.putExtra("visit_user_name", codename);
                groupChatIntent.putExtra("visit_image",  retImage[0]);
                startActivity(groupChatIntent);
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    private void InitializeFields() {
        recyclerView = (findViewById(R.id.recycler_view));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Messages").child("Groups");
        databaseReference.keepSynced(true);

    }
    public void onBackPressed(){
        if (firebaseAuth.getCurrentUser() != null) {

            currentUser = firebaseAuth.getCurrentUser();
            userId = firebaseAuth.getCurrentUser().getUid();

            firebaseDatabase = FirebaseDatabase.getInstance();
            myRef = FirebaseDatabase.getInstance().getReference();

            DatabaseReference reference = myRef.child("Users").child(userId);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    fullname = dataSnapshot.child("fullname").getValue().toString();
                    codename = dataSnapshot.child("codename").getValue().toString();

                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.hasChild("Images")) {
                            retImage[0] = dataSnapshot.child("Images").getValue().toString();
                        }
                    }

                            Intent groupChatIntent = new Intent(ViewGroupActivity.this, ChatActivity.class);
                            groupChatIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            groupChatIntent.putExtra("visit_user_id", userId);
                            groupChatIntent.putExtra("visit_user_name", codename);
                            groupChatIntent.putExtra("visit_image", retImage[0]);
                            startActivity(groupChatIntent);
                            finish();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
    private void subscribeToNotification() {
        subscriptionReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> subscription;
                if(snapshot.exists()) {
                    subscription = (ArrayList<String>)snapshot.getValue();
                }else {
                    subscription = new ArrayList<String>();
                }
                    for(String group: groupChatNames){
                        if(!subscription.contains(group)){
                            subscription.add(group);

                            Log.e(TAG,group+"Sub");
                        }
                        subscribeToTopic(group);
                    }
                for(String group: subscription){
                    if(!groupChatNames.contains(group)){
                        unsubscribeTopic(group);
                        Log.e(TAG,group+groupChatNames.size());
                    }
                }
                    subscriptionReference.setValue(subscription).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG,"Subscribed to group notification");

                        }
                    });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void subscribeToTopic(final String group){
        FirebaseMessaging.getInstance().subscribeToTopic(getString(R.string.chat)+group)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        String msg = getString(R.string.msg_subscribed);
                        if (!task.isSuccessful()) {
                            msg = getString(R.string.msg_subscribe_failed);
                        }
                        Log.d(TAG, msg+group);
                    }
                });
    }

    private void unsubscribeTopic(final String currentGroupName) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(getString(R.string.chat) + currentGroupName)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = getString(R.string.msg_unsubscribed);
                        if (!task.isSuccessful()) {
                            msg = getString(R.string.msg_unsubscribe_failed);
                        }
                        Log.d(TAG, msg+currentGroupName);
                    }
                });
    }
}


