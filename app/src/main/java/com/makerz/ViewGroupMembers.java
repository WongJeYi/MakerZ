package com.makerz;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.makerz.model.GroupMembers;
import com.makerz.adapter.ViewMemberAdapter;
import com.makerz.model.user;

import java.util.ArrayList;
import java.util.List;

// This activity is used to kick participants/group members out of the group.
// Only users which are in the group will be displayed.

public class ViewGroupMembers extends AppCompatActivity {

    // This is used to check (if currentGroupName exist on each user) is done.
    Integer count = 0;

    private final List<GroupMembers> arrayList = new ArrayList<>();
    private String currentGroupName,messageReceiverID, messageReceiverName, messageReceiverImage;
    private String userId, fullname, codename;
    private String[] retImage = {"default_image"};

    private DatabaseReference databaseReference, myRef;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser currentUser;
    private StorageReference imageReference, storageReference;

    private ViewMemberAdapter viewMemberAdapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;

    private LinearLayout add_participants, exit_group;
    private TextView participants_count, leave_group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_group_members);

        // Declaration of elements.
        setupUIViews();

        // Display back button on the toolbar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get current user's info.
        final StorageReference[] storageReference = {FirebaseStorage.getInstance().getReference("Images")};
        imageReference = storageReference[0].child("Profile Picture").child(userId + "/" + "profilepic.jpg");
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {

            firebaseAuth = FirebaseAuth.getInstance();
            currentUser = firebaseAuth.getCurrentUser();
            userId = firebaseAuth.getCurrentUser().getUid();

            firebaseDatabase = FirebaseDatabase.getInstance();
            myRef = FirebaseDatabase.getInstance().getReference();
            viewMemberAdapter = new ViewMemberAdapter(arrayList, currentGroupName, userId,this);
            recyclerView.setAdapter(viewMemberAdapter);

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

                    RetrieveAndDisplayGroupMembers();
                    if(dataSnapshot.child("Groups").child(currentGroupName).exists()) {

                        String status = dataSnapshot.child("Groups").child(currentGroupName).getValue().toString();
                        if (status.equals("Owner")) {

                            // If owner press LinearLayout exit_group, the group that the owner is in
                            // will be deleted from the firebase.
                            // Then, pass user's values and move to ChatActivity.

                            leave_group.setText("Delete Group");
                            exit_group.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    myRef.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            count = 0;
                                            final long target = dataSnapshot.getChildrenCount();

                                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                count++;
                                                if (ds.child("Groups").child(currentGroupName).exists()) {
                                                    ds.child("Groups").child(currentGroupName).getRef().removeValue();

                                                    if (count >= target) {
                                                        count = 0;
                                                        myRef.child("Messages").child("Groups").child(currentGroupName).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()) {
                                                                    Intent groupChatIntent = new Intent(ViewGroupMembers.this, ChatActivity.class);
                                                                    groupChatIntent.putExtra("groupName", "MakerZ");
                                                                    groupChatIntent.putExtra("visit_user_id", messageReceiverID);
                                                                    groupChatIntent.putExtra("visit_user_name", messageReceiverName);
                                                                    groupChatIntent.putExtra("visit_image", messageReceiverImage);
                                                                    startActivity(groupChatIntent);
                                                                    finish();
                                                                }
                                                            }
                                                        });
                                                    }
                                                } else {

                                                    if (count >= target) {
                                                        count = 0;
                                                        myRef.child("Messages").child("Groups").child(currentGroupName).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()) {
                                                                    Intent groupChatIntent = new Intent(ViewGroupMembers.this, ChatActivity.class);
                                                                    groupChatIntent.putExtra("groupName", "MakerZ");
                                                                    groupChatIntent.putExtra("visit_user_id", messageReceiverID);
                                                                    groupChatIntent.putExtra("visit_user_name", messageReceiverName);
                                                                    groupChatIntent.putExtra("visit_image", messageReceiverImage);
                                                                    startActivity(groupChatIntent);
                                                                    finish();
                                                                }
                                                            }
                                                        });

                                                    }
                                                }

                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                }
                            });
                        } else if (status.equals("Member")) {

                            // If member press LinearLayout exit_group, the member's codename
                            // will be deleted from anything related to the current group's firebase.
                            // Then, pass user's values and move to ChatActivity.
                            leave_group.setText("Leave Group");
                            exit_group.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    myRef.child("Messages").child("Groups").child(currentGroupName).child("Group Members").child("Members").child(fullname).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                myRef.child("Users").child(userId).child("Groups").child(currentGroupName).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()) {
                                                            Intent groupChatIntent = new Intent(ViewGroupMembers.this, ChatActivity.class);
                                                            groupChatIntent.putExtra("groupName", "MakerZ");
                                                            groupChatIntent.putExtra("visit_user_id", messageReceiverID);
                                                            groupChatIntent.putExtra("visit_user_name", messageReceiverName);
                                                            groupChatIntent.putExtra("visit_image", messageReceiverImage);
                                                            startActivity(groupChatIntent);
                                                            finish();
                                                        }
                                                    }
                                                });
                                            }

                                        }
                                    });

                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        // If LinearLayout add_participant is pressed, pass user's values and move to AddGroupMembers activity.
        add_participants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent groupChatIntent = new Intent(ViewGroupMembers.this, AddGroupMembers.class);
                groupChatIntent.putExtra("groupName", currentGroupName);
                groupChatIntent.putExtra("visit_user_id",  messageReceiverID);
                groupChatIntent.putExtra("visit_user_name",  messageReceiverName);
                groupChatIntent.putExtra("visit_image", messageReceiverImage);
                startActivity(groupChatIntent);
                finish();
            }
        });


    }

    // Retrieve codename of the users who are in the group and add to arrayList,
    // which will pass to ViewMemberAdapter.
    private void RetrieveAndDisplayGroupMembers()
    {
        databaseReference.child("Group Members").child("Members").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                GroupMembers members = dataSnapshot.getValue(GroupMembers.class);
                arrayList.add(members);

                if (arrayList.size() > 1){
                participants_count.setText(arrayList.size() + " members");
                }else{
                    participants_count.setText(arrayList.size() + " member");
                }
                viewMemberAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    // If back button on toolbar is pressed, pass user's values and move to PrivateGroupChatActivity.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                Intent groupChatIntent = new Intent(ViewGroupMembers.this, PrivateGroupChatActivity.class);
                groupChatIntent.putExtra("groupName", currentGroupName);
                groupChatIntent.putExtra("visit_user_id",  messageReceiverID);
                groupChatIntent.putExtra("visit_user_name",  messageReceiverName);
                groupChatIntent.putExtra("visit_image", messageReceiverImage);
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

    private void setupUIViews() {

        add_participants = (findViewById(R.id.add_participants));
        exit_group = (findViewById(R.id.exit_group));
        participants_count = (findViewById(R.id.participants_count));
        leave_group = (findViewById(R.id.leave_group));

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage = getIntent().getExtras().get("visit_image").toString();

        recyclerView = (findViewById(R.id.recycler_view_group_members));
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        databaseReference = FirebaseDatabase.getInstance().getReference("Messages").child("Groups").child(currentGroupName);
        databaseReference.keepSynced(true);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    public void onBackPressed(){
        Intent intent = new Intent(ViewGroupMembers.this, ViewGroupActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
