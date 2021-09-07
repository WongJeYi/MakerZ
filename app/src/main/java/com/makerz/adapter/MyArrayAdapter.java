package com.makerz.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.makerz.R;
import com.makerz.model.MyDataModel;
import com.facebook.drawee.view.SimpleDraweeView;


import java.util.List;

// This adapter is used to display payment status and name of each MakerZ member.
// Data values in List<MyDataModel> modelList are taken from ViewGroupMembers activity.
public class MyArrayAdapter extends ArrayAdapter<MyDataModel> {

    private List<MyDataModel> modelList;
    private Context context;
    private LayoutInflater mInflater;

    // Constructors
    // This passes values from ListMakerZ activity line 61:
    // adapter = new MyArrayAdapter(this, list);
    public MyArrayAdapter(Context context, List<MyDataModel> objects) {
        super(context, 0, objects);
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        modelList = objects;
    }

    @Override
    public MyDataModel getItem(int position) {
        return modelList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder vh;
        if (convertView == null) {
            View view = mInflater.inflate(R.layout.layout_row_view, parent, false);
            vh = ViewHolder.create((RelativeLayout) view);
            view.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        MyDataModel item = getItem(position);

        // Set MakerZ members' name and payment status
        vh.textViewName.setText(item.getName());
        vh.textViewFees.setText(item.getFees());

        return vh.rootView;
    }

    /*** ViewHolder class for layout.<br /> * <br />
     * Auto-created on 2016-01-05 00:50:26 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)*/
    private static class ViewHolder {
        public final RelativeLayout rootView;

        public final TextView textViewName;
        public final TextView textViewFees;

        private ViewHolder(RelativeLayout rootView, TextView textViewName, TextView textViewFees) {
            this.rootView = rootView;
            this.textViewName = textViewName;
            this.textViewFees = textViewFees;
        }

        public static ViewHolder create(RelativeLayout rootView) {
            TextView textViewName = (TextView) rootView.findViewById(R.id.textViewName);
            TextView textViewFees = (TextView) rootView.findViewById(R.id.textViewFees);
            return new ViewHolder(rootView, textViewName, textViewFees);
        }
    }
}
