package com.makerz.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.makerz.R;
import com.makerz.model.MakerEvent;

import java.util.List;
// AttendanceAdapter is the adapter for maker event list under attendance calendar
public class AttendanceAdapter extends ArrayAdapter<MakerEvent> {


        private List<MakerEvent> list;
        private LayoutInflater mInflater;

        public AttendanceAdapter(Context context, List<MakerEvent> list) {
            super(context, R.layout.row, list);
            this.mInflater = LayoutInflater.from(context);
            this.list = list;
        }

        static class ViewHolder {
            TextView text;

        }

        public void addItems(List<MakerEvent> list) {
            this.list.clear();
            this.list.addAll(list);
            notifyDataSetChanged();

        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                // initiate view
                convertView = mInflater.inflate(R.layout.row, parent,false);
                viewHolder = new ViewHolder();
                viewHolder.text = (TextView) convertView.findViewById(R.id.group_name);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            // set title to the list
            viewHolder.text.setText(list.get(position).getTitle());

            return convertView;
        }
        public void clearData() {
        // clear the data
        list.clear();
        }

    }