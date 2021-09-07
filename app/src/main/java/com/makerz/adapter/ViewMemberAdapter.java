package com.makerz.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.makerz.MyRecycleViewClickListener;
import com.makerz.R;
import com.makerz.ViewGroupActivity;
import com.makerz.ViewGroupMembers;
import com.makerz.model.GroupMembers;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

// This adapter is used to display users who are members of a specific group.
// Data values in List<GroupMembers> membersList are taken from ViewGroupMembers activity.

public class ViewMemberAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

   private List<GroupMembers> membersList;
   private String currentGroupName, selectedUserCodenamestatus, selectedUserCodename;
   private String userId, codename, fullname;

   private DatabaseReference myRef;
   private StorageReference imageReference;

   final StorageReference storageReference = FirebaseStorage.getInstance().getReference("Images");
   private Context context;

    // Constructors
    // This passes values from ViewGroupMembers activity line 88:
    // viewMemberAdapter = new ViewMemberAdapter(arrayList, currentGroupName, userId);
    public ViewMemberAdapter(List<GroupMembers> membersList, String currentGroupName, String userId, Context context)
    {
        this.membersList = membersList;
        this.currentGroupName = currentGroupName;
        this.userId = userId;
        this.context=context;
    }

    public class ViewMemberChatViewHolder extends RecyclerView.ViewHolder {
        TextView usercodename,userstatus;
        ImageView userimage;

        public ViewMemberChatViewHolder(View itemView) {
            super(itemView);

            usercodename = itemView.findViewById(R.id.textView_group_members);
            userimage = itemView.findViewById(R.id.imageView_group_member);
            userstatus = itemView.findViewById(R.id.textView_group_members_status);
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View viewMembers = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_row_view_group_members, parent, false);
        RecyclerView.ViewHolder viewHolder = new ViewMemberChatViewHolder(viewMembers);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
           configureViewMemberViewHolder((ViewMemberChatViewHolder) holder, position);
    }

    @Override
    public int getItemCount() {
        return membersList.size();
    }

    private void configureViewMemberViewHolder(final ViewMemberChatViewHolder viewMemberViewHolder, final int position)
    {
        final GroupMembers members = membersList.get(position);

        // Set codename & status
        viewMemberViewHolder.usercodename.setText(members.getCodename());
        viewMemberViewHolder.userstatus.setText(members.getStatus());

            // Only owner can kick members out of the group.
            // However, owner cannot kick him/herself out of the group.
            // And can only delete the group, which is coded in ViewGroupMembers activity.
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child("Groups").child(currentGroupName).exists()) {

                        String status = dataSnapshot.child("Groups").child(currentGroupName).getValue().toString();
                    if (status.equals("Owner")) {
                        viewMemberViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                selectedUserCodenamestatus = viewMemberViewHolder.userstatus.getText().toString();
                                selectedUserCodename = viewMemberViewHolder.usercodename.getText().toString();

                                if (!selectedUserCodenamestatus.equals("Owner")) {

                                    CharSequence options[] = new CharSequence[]
                                            {
                                                    "Remove",
                                                    "Cancel"
                                            };

                                    AlertDialog.Builder builder = new AlertDialog.Builder(viewMemberViewHolder.itemView.getContext());
                                    builder.setTitle("Remove user from the group?");
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (i == 0) {

                                            removeUserFromGroup(position, viewMemberViewHolder);

                                            } else if (i == 1) {
                                                // Nothing happens
                                            }

                                        }
                                    });
                                    builder.show();
                                }
                            }
                        });
                    }


                }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        // Set picture

        myRef = FirebaseDatabase.getInstance().getReference().child("Users");

        try {
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String codename = ds.child("codename").getValue(String.class);

                        if (codename.equals(members.getCodename())) {
                            imageReference = storageReference.child("Profile Picture").child(ds.getKey() + "/" + "profilepic.jpg");

                            if (imageReference == null) {
                            } else {
                                imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        Picasso.get().load(uri).error(R.drawable.profile3).fit().centerCrop().into(viewMemberViewHolder.userimage);

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
    }

    // Remove user from the group.
    private void removeUserFromGroup(final int position, final ViewMemberChatViewHolder viewMemberViewHolder) {

        myRef = FirebaseDatabase.getInstance().getReference();
        myRef.child("Messages").child("Groups").child(currentGroupName).child("Group Members").child("Members").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds :dataSnapshot.getChildren())
                {
                    String checkcodename = ds.child("codename").getValue().toString();

                    if(checkcodename.equals(selectedUserCodename))
                    {
                        String deleteFullname = ds.getKey();
                        myRef.child("Messages").child("Groups").child(currentGroupName).child("Group Members").child("Members").child(deleteFullname).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    Intent intent = new Intent(context, ViewGroupActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(intent);
                                    ((Activity) context).finish();
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        myRef.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds :dataSnapshot.getChildren())
                {
                    String deleteuser = ds.child("codename").getValue().toString();

                    if(deleteuser.equals(selectedUserCodename))
                    {
                        String deleteuserID = ds.getKey();
                        myRef.child("Users").child(deleteuserID).child("Groups").child(currentGroupName).removeValue();
                        membersList.remove(position);

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


