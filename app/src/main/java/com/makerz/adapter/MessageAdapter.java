package com.makerz.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makerz.ChatActivity;
import com.makerz.MainActivity;
import com.makerz.R;
import com.makerz.model.AllMethods;
import com.makerz.model.ActivityMessage;
import com.makerz.model.user;
import com.makerz.util.FirebaseUtil;

import java.util.List;
import java.util.Objects;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageAdapterViewHolder>
{
   Context context;
   List<ActivityMessage>  messages;
   private DatabaseReference myRef;
   private FirebaseAuth firebaseAuth;
   private FirebaseDatabase firebaseDatabase;
   String userId;
   String codename;

   public MessageAdapter(Context context,List<ActivityMessage>  messages,DatabaseReference myRef)
   {
       this.context =  context;
       this.messages =  messages;
       this.myRef =  myRef;
   }

    @NonNull
    @Override
    public MessageAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.item_message,parent,false);
       return new MessageAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageAdapterViewHolder holder, int position) {
        final ActivityMessage message = messages.get(position);

        myRef = FirebaseDatabase.getInstance().getReference("Messages");

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = firebaseAuth.getInstance().getCurrentUser().getUid();

        } else {

        }


        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //get id
                codename = dataSnapshot.child("Users").child(userId).child("Codename").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (message.getCodename().equals(AllMethods.codename)) {
            holder.tvTitle.setText("You: " + message.getMessage());
            holder.tvTitle.setGravity(Gravity.START);
            holder.ii.setBackgroundColor(Color.parseColor("585353"));
        } else {

            holder.tvTitle.setText(codename + ":" + message.getMessage());
            holder.ibDelete.setVisibility(View.GONE);

    }





    }

    public void onCancelled(@NonNull DatabaseError databaseError) {

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class MessageAdapterViewHolder extends RecyclerView.ViewHolder
 {
     TextView tvTitle;
     ImageButton ibDelete;
     LinearLayout ii;

     public MessageAdapterViewHolder(View itemView)
     {
         super(itemView);
         tvTitle = itemView.findViewById(R.id.tvTitle);
         ibDelete = itemView.findViewById(R.id.ibDelete);
         ii = itemView.findViewById(R.id.iiMessage);

         ibDelete.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 myRef.child(messages.get(getAdapterPosition()).getKey()).removeValue();
             }
         });


     }
 }

}
