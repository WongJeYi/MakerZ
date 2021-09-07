package com.makerz;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.makerz.adapter.AddMemberAdapter;
import com.makerz.model.GroupMembers;
import com.makerz.model.user;
import com.makerz.util.FirebaseUtil;

import java.util.ArrayList;
import java.util.List;

// This activity is used to add participants/group members to a group.
// Only users which are not in the group will be displayed.

public class AddGroupMembers extends AppCompatActivity {

    private final List<String> arrayList = new ArrayList<>();
    private final List<String> tokenArrayList = new ArrayList<>();
    private String currentGroupName,messageReceiverID, messageReceiverName, messageReceiverImage;
    private String fullname, codename, userId;
    private String[] retImage = {"default_image"};

    private DatabaseReference myRef,tokenRef;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser currentUser;

    private AddMemberAdapter addMemberAdapter;
    private RecyclerView FindParticipantRecyclerList;
    private LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group_members);

        // Display back button on the toolbar & set title as "Add Participants".
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.setTitle("Add Participants");

        // Values passed from ViewGroupMembers activity.
        currentGroupName = getIntent().getExtras().get("groupName").toString();
        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage = getIntent().getExtras().get("visit_image").toString();

        // Declaration of elements.

        firebaseDatabase = FirebaseDatabase.getInstance();

        myRef = firebaseDatabase.getReference();
        tokenRef = firebaseDatabase.getReference().child("Token");
        FindParticipantRecyclerList = (findViewById(R.id.rvMessage_add_participant));
        linearLayoutManager = new LinearLayoutManager(this);
        FindParticipantRecyclerList.setLayoutManager(linearLayoutManager);

        databaseReference = firebaseDatabase.getReference("Users");
        databaseReference.keepSynced(true);

        firebaseAuth = FirebaseAuth.getInstance();

        // Get current user's info.
        if (firebaseAuth.getCurrentUser() != null) {

            currentUser = firebaseAuth.getCurrentUser();
            userId = firebaseAuth.getCurrentUser().getUid();


            firebaseDatabase = FirebaseDatabase.getInstance();
            myRef = firebaseDatabase.getReference();

            // Passes values, arrayList, currentGroupName and userId to AddMemberAdapter.
            addMemberAdapter = new AddMemberAdapter(arrayList, currentGroupName,userId);
            FindParticipantRecyclerList.setAdapter(addMemberAdapter);

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
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    // Retrieve codename of the users who are not in the group and add to arrayList,
    // which will pass to AddMemberAdapter.
    private void RetrieveAndDisplayGroupMembers() {

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                arrayList.clear();
                tokenArrayList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    if (ds.child("Groups").exists())
                    {
                        String groupname = ds.child("Groups").getValue().toString();

                        if(!groupname.contains(currentGroupName))
                        {
                            arrayList.add(ds.child("codename").getValue().toString());
                        }
                    }
                    else if (!ds.child("Groups").exists())
                        {
                            arrayList.add(ds.child("codename").getValue().toString());
                        }
                }
                addMemberAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        getToken();
    }

    private void getToken() {
    }

    // If back button on toolbar is pressed, pass user's values and move to ViewGroupMembers activity.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                Intent groupChatIntent = new Intent(AddGroupMembers.this, ViewGroupMembers.class);
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

    @Override
    protected void onStart() {
        super.onStart();

    }
    @Override
    public void onBackPressed(){
        Intent intent = new Intent(AddGroupMembers.this, ViewGroupActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}
