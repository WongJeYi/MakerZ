package com.makerz.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CalendarContract;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makerz.R;
import com.makerz.model.Data.MySingleton;
import com.makerz.model.MakerEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import static java.lang.Integer.parseInt;

// Adapter for recyclerview in contentstuff activity shared by trip, activity and contest
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.MyViewHolder> {
    Context mContext;
    private static final String MAKERZ_PICTURE_FOLDER="Picture";
    private List<MakerEvent> makerEventList;
    private MakerEvent mMakerEvent;
    private FirebaseAuth firebaseAuth;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, deadline;
        public ImageView thumbnail, overflow,verified;
        public MyViewHolder(View view) {
            super(view);
            // initialize card holder
            title = (TextView) view.findViewById(R.id.event_title);
            deadline = (TextView) view.findViewById(R.id.deadline);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            overflow = (ImageView) view.findViewById(R.id.overflow);
            verified = (ImageView) view.findViewById(R.id.verified);
        }
    }
    public EventAdapter(Context mContext, List<MakerEvent> makerEventList) {
        this.mContext = mContext;
        // makerEventList is the event of either one of trip, contest or activity
        this.makerEventList = makerEventList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // get cardview layout
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_cards, parent, false);

        return new MyViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        // makerEvent is the event to be displayed
        final MakerEvent makerEvent = makerEventList.get(position);
        mMakerEvent=makerEvent;
        String imgPath;
        String encodedImage;
        // set text to the cardview
            holder.title.setText(makerEvent.getTitle());
            holder.deadline.setText(makerEvent.getDeadline());
            // loading album cover using Glide library
            encodedImage = makerEvent.getThumbnail();
            Log.d("Img","it is"+encodedImage);
            // default pictures
            if(encodedImage==null){
                if(makerEvent.getType().equals("Trip")) {
                    Glide.with(mContext).load(R.drawable.trip_event).into(holder.thumbnail);
                }else if(makerEvent.getType().equals("Activity")) {
                    Glide.with(mContext).load(R.drawable.activity_event).into(holder.thumbnail);
                }else if(makerEvent.getType().equals("Contest")) {
                    Glide.with(mContext).load(R.drawable.contest_event).into(holder.thumbnail);
                }

            }
            else {
                // load pictures
                byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
                imgPath = saveToInternalStorage(bytes, makerEvent.getTitle()+makerEvent.getThumbnail().substring(0,10));
                Glide.with(mContext).load(imgPath).into(holder.thumbnail);
            }
            holder.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                // open the web url with web browser
                    Intent open;
                    String Url = makerEvent.getEvent_url();
                    if (Url == null) {
                    } else if (!Url.startsWith("https://") && !Url.startsWith("http://")) {
                        Url = "http://" + Url;
                    }
                    open = new Intent(Intent.ACTION_VIEW);
                    open.setData(Uri.parse(Url));
                    Bundle args = new Bundle();
                    ContextCompat.startActivity(mContext, open, args);


                }
            });
            // to show the menu of @description or @setReminder
            holder.overflow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPopupMenu(holder.overflow, makerEvent);
                }
            });
            if(makerEvent.isVerified()&&MySingleton.isAdmin) {
                Glide.with(mContext).load(R.drawable.tick).into(holder.verified);
            }

    }

    @Override
    public int getItemCount() {
        return makerEventList.size();
    }

    private void showPopupMenu(View view, final MakerEvent makerEvent) {
        // inflate menu
        PopupMenu popup = new PopupMenu(mContext, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu2, popup.getMenu());
        if(MySingleton.isAdmin) {
            popup.getMenu().findItem(R.id.action_verified).setEnabled(true);
            popup.getMenu().findItem(R.id.action_undo_verified).setEnabled(true);
            popup.getMenu().findItem(R.id.action_delete).setEnabled(true);
            popup.getMenu().findItem(R.id.action_delete).setVisible(true);
            popup.getMenu().findItem(R.id.action_verified).setVisible(true);
            popup.getMenu().findItem(R.id.action_undo_verified).setVisible(true);
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                final DatabaseReference databaseRefer = firebaseDatabase.getReference().child("Events");
                switch (menuItem.getItemId()) {
                    case R.id.action_set_reminder:
                        Log.d("URl",makerEvent.getTime());
                        setReminder(makerEvent);
                        Toast.makeText(mContext, makerEvent.getEvent_url(), Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.action_load_description:
                        Toast.makeText(mContext, makerEvent.getDescription(), Toast.LENGTH_LONG).show();
                        return true;
                    case R.id.action_verified:
                        makerEvent.setVerified(true);
                        databaseRefer.child(makerEvent.getTitle()).setValue(makerEvent);
                        databaseRefer.child(makerEvent.getTitle()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Toast.makeText(mContext, makerEvent.getTitle()+" is verified.", Toast.LENGTH_LONG).show();
                                ((Activity)mContext).finish();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        return true;
                    case R.id.action_undo_verified:
                        makerEvent.setVerified(false);
                        databaseRefer.child(makerEvent.getTitle()).setValue(makerEvent);
                        databaseRefer.child(makerEvent.getTitle()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Toast.makeText(mContext, "Verification of "+makerEvent.getTitle()+" is removed.", Toast.LENGTH_LONG).show();
                                ((Activity)mContext).finish();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        return true;
                    case R.id.action_delete:
                        databaseRefer.child(makerEvent.getTitle()).getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                databaseRefer.child(makerEvent.getTitle()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        Toast.makeText(mContext,"You removed "+ makerEvent.getTitle(), Toast.LENGTH_LONG).show();
                                        ((Activity)mContext).finish();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }
                        });
                        return true;
                    default:
                }
                return false;
            }
        });
        popup.show();
    }
    private void setReminder(MakerEvent event) {
        String url=event.getEvent_url();
        Uri webpage = Uri.parse(url);

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            webpage = Uri.parse("http://" + url);
        }
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI);
        intent.putExtra(CalendarContract.Events.TITLE,event.getTitle());
        intent.putExtra(CalendarContract.Events.DESCRIPTION,event.getDescription());
        intent.putExtra(CalendarContract.Events.HAS_ALARM,true);
        ZoneId zoneId;
        long starttime=0;
        if(event.getTime()!=null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                String time=event.getTime();
                String date= event.getDeadline()+" "+time;
                DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                zoneId = ZoneId.systemDefault(); // or: ZoneId.of("Europe/Oslo");
                 starttime= LocalDateTime.parse(date, formatter).atZone(zoneId)
                        .toInstant()
                        .toEpochMilli();
                Log.d("Sdate", String.valueOf(starttime));
            }else{
                Toast.makeText(mContext,"function not supported by device.",Toast.LENGTH_SHORT).show();
                return;
            }

        }
        long endtime=0;
        if(event.getEndtime()!=null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                String time=event.getEndtime();
                String date= event.getDeadline()+" "+time;
                DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                zoneId = ZoneId.systemDefault(); // or: ZoneId.of("Europe/Oslo");
                endtime= LocalDateTime.parse(date, formatter).atZone(zoneId)
                        .toInstant()
                        .toEpochMilli();
                Log.d("Sdates", String.valueOf(endtime));
            }else{
                Toast.makeText(mContext,"function not supported by device.",Toast.LENGTH_SHORT).show();
                return;
            }

        }
        if(starttime>endtime){
            endtime=endtime+1000*60*60*24;
        }
        intent.putExtra(CalendarContract.Events.ALL_DAY,false);
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,starttime);
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,endtime);
        intent.putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        intent.putExtra(Intent.EXTRA_EMAIL,email);
        if(intent.resolveActivity(mContext.getPackageManager())!=null){
            mContext.startActivity(intent);
        }else{
            Toast.makeText(mContext,"function not supported by device.",Toast.LENGTH_SHORT).show();
        }

    }

    private String getFilename(String name) {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, MAKERZ_PICTURE_FOLDER);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath() + "/"+name ;
    }
    private String saveToInternalStorage(byte[] bytes, String filename){
        ContextWrapper cw = new ContextWrapper(mContext);
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
            Log.d("s",e.toString());
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("s",e.toString());
            }
        }
        return mypath.toString();
    }
}

