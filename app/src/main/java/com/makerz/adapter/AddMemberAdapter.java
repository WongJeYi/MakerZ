package com.makerz.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AdapterListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.makerz.R;
import com.makerz.model.GroupMembers;
import com.makerz.model.user;
import com.squareup.picasso.Picasso;

import java.util.List;

// This adapter is used to display users who are not members of the specific group.
// Data values in List<String> nonMembersList are taken from AddGroupMembers activity.

public class AddMemberAdapter extends  RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<String>  nonMembersList;
    private String currentGroupName, userId;
    int i = 0;

    private DatabaseReference myRef, databaseReference;
    private StorageReference imageReference;

    final StorageReference storageReference = FirebaseStorage.getInstance().getReference("Images");

    // Constructors
    // This passes values from AddGroupMembers activity line 89:
    // addMemberAdapter = new AddMemberAdapter(arrayList, currentGroupName,userId);
    public AddMemberAdapter (List<String>nonMembersList, String currentGroupName, String userId)
    {
        this.nonMembersList = nonMembersList;
        this.currentGroupName = currentGroupName;
        this.userId = userId;
    }

    public class AddMemberChatViewHolder extends RecyclerView.ViewHolder
    {
        TextView usercodename;
        ImageView userimage;

        public AddMemberChatViewHolder (View itemView) {
        super(itemView);
            usercodename = itemView.findViewById(R.id.textView_add_group_members);
            userimage = itemView.findViewById(R.id.imageView_add_group_member);

        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View viewMembers = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_row_view_add_members, parent, false);
        RecyclerView.ViewHolder viewHolder = new AddMemberAdapter.AddMemberChatViewHolder(viewMembers);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        configureAddMemberViewHolder((AddMemberChatViewHolder) holder, position);
    }

    @Override
    public int getItemCount() {
        return nonMembersList.size();
    }

    private void configureAddMemberViewHolder(final AddMemberChatViewHolder addMemberViewHolder, final int position)
    {
        // This is used so that the Toast on line 187 is only displayed,
        // when owner of the group add new user to the group.
        i = 0;

        // Set codename
        final String nonMember = nonMembersList.get(position);
        addMemberViewHolder.usercodename.setText(nonMember);

        // Set picture
        myRef = FirebaseDatabase.getInstance().getReference();

        try {
                myRef.child("Users").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ds : dataSnapshot.getChildren())
                        {
                            String codename = ds.child("codename").getValue(String.class);

                            if(codename.equals(nonMember))
                            {
                                imageReference = storageReference.child("Profile Picture").child(ds.getKey() + "/" + "profilepic.jpg");

                                if (imageReference == null) { }
                                else {
                                    imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {

                                            Picasso.get().load(uri).error(R.drawable.profile3).fit().centerCrop().into(addMemberViewHolder.userimage);

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

            }catch(Exception e){
                e.getLocalizedMessage();
            }


          // Only owner of the group can add new users into the group,
          // else Toast on line 220 will be shown to tell members only owner can add new members.
          DatabaseReference reference = myRef.child("Users").child(userId);
          reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.child("Groups").child(currentGroupName).exists())
                {
                    String status = dataSnapshot.child("Groups").child(currentGroupName).getValue().toString();

                    if (status.equals("Owner")) {

                        addMemberViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                i = 1;

                                final String selectedUserCodename = nonMembersList.get(position);

                                myRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                    {

                                        if (dataSnapshot.hasChild("Users"))
                                        {
                                            databaseReference = myRef.child("Users");
                                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                    for (DataSnapshot ds : dataSnapshot.getChildren())
                                                    {
                                                        String userFullnameForGroupChat = ds.child("fullname").getValue(String.class);
                                                        String userCodenameForGroupChat = ds.child("codename").getValue(String.class);
                                                        String userIdForGroupChat = ds.getKey();
                                                        if (selectedUserCodename.equals(userCodenameForGroupChat))
                                                        {
                                                            myRef.child("Messages").child("Groups").child(currentGroupName).child("Group Members").child("Members").child(userFullnameForGroupChat).child("codename").setValue(userCodenameForGroupChat);
                                                            myRef.child("Messages").child("Groups").child(currentGroupName).child("Group Members").child("Members").child(userFullnameForGroupChat).child("status").setValue("Member");
                                                            myRef.child("Users").child(userIdForGroupChat).child("Groups").child(currentGroupName).setValue("Member");

                                                            if (i == 1)
                                                            {
                                                                Toast.makeText(addMemberViewHolder.itemView.getContext(), userCodenameForGroupChat + " is added to the group.", Toast.LENGTH_SHORT).show();
                                                                i = 0;
                                                            }
                                                            nonMembersList.remove(selectedUserCodename);
                                                            notifyDataSetChanged();

                                                        }

                                                    }

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        });

                    }
                    else
                        {
                            addMemberViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Toast.makeText(addMemberViewHolder.itemView.getContext(),  "Only owner can add new members to the group.", Toast.LENGTH_SHORT).show();

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

}
