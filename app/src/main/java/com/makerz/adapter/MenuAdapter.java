package com.makerz.adapter;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.makerz.MenuActivity;
import com.makerz.R;
import com.makerz.model.MakerEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

// MenuAdapter is the adapter of MenuActivity for the trip, activity and contest horizontal recyclerview
public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MyViewHolder> {
    private MenuActivity mMenuActivity;
    private ArrayList<MakerEvent> mDataset;
    private static final String MAKERZ_PICTURE_FOLDER="Picture";

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView mImageView;
        public MyViewHolder(View v) {
            super(v);
            // initialize view with picture
            mImageView = v.findViewById(R.id.picture);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MenuAdapter(MenuActivity menuActivity, ArrayList<MakerEvent> myDataset) {
        mMenuActivity = menuActivity;
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        // create a new view
        View v = (LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_image_view, parent, false));

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent open;
                String Url = mDataset.get(position).getEvent_url();
                if (Url == null) {
                } else if (!Url.startsWith("https://") && !Url.startsWith("http://")) {
                    Url = "http://" + Url;
                }
                open = new Intent(Intent.ACTION_VIEW);
                open.setData(Uri.parse(Url));
                open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Bundle args = new Bundle();
                ContextCompat.startActivity(mMenuActivity.getApplicationContext(), open, args);

            }
        });
        String encodedimage=mDataset.get(position).getThumbnail();
        String title=mDataset.get(position).getTitle();
        if(encodedimage!=null) {
            byte[] bytes = Base64.decode(encodedimage, Base64.DEFAULT);
            String imgPath = saveToInternalStorage(bytes, title+encodedimage.substring(0,10));
            Glide.with(mMenuActivity.getApplicationContext()).load(imgPath).into(holder.mImageView);
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
    private String saveToInternalStorage(byte[] bytes, String filename){
        ContextWrapper cw = new ContextWrapper(mMenuActivity);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir(MAKERZ_PICTURE_FOLDER, Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,filename+".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            fos.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("s",e.toString());
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("s",e.toString());
            }
        }
        return mypath.toString();
    }
}
