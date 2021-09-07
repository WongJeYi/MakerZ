package com.makerz.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.makerz.R;

import java.util.ArrayList;


// This adapter is used to display group names which the user is a member/owner of in a recycler view.
// Data values in ArrayList<String> list are taken from ViewGroupActivity.

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyHolder> {

        private ArrayList<String> list;

        // Constructors
        // This passes values from ViewGroupMembers, activity line 111:
        // CustomAdapter adapter = new CustomAdapter(groupChatNames);
        public CustomAdapter(ArrayList<String> list) {
            this.list = list;
        }

        @Override
        public CustomAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false);
            MyHolder holder = new MyHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(MyHolder holder, int position) {
            holder.setText(list.get(position));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }


        class MyHolder extends RecyclerView.ViewHolder {
            TextView nameTextView;

            public MyHolder(View itemView) {
                super(itemView);
                nameTextView = (TextView) itemView.findViewById(R.id.group_name);
            }

            // Set group name
            public void setText(String userCodenameForGroupChat) {
                nameTextView.setText(userCodenameForGroupChat);
            }
        }
    }

