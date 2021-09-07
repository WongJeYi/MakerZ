package com.makerz.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.util.DateInterval;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.makerz.PrivateGroupChatActivity;
import com.makerz.R;
import com.makerz.ZoomPicActivitiy;
import com.makerz.model.Message;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.datatype.Duration;

// This adapter is used to display chat messages.
// Data values in List<Message> userMessageList are taken from PrivateGroupChatActivity.

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Context context;
    private List<Message> userMessageList;
    private String codename, currentGroupName;

    public static final int MSG_TYPE_RIGHT = 1;
    public static final int MSG_TYPE_LEFT = 0;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference myRef;
    private StorageReference imageReference;
    private FirebaseDatabase firebaseDatabase;

    final StorageReference storageReference = FirebaseStorage.getInstance().getReference("Images");
    private static boolean playing;

    // Constructors
    // This passes values from PrivateGroupChatActivity line :
    //
    public ChatAdapter(Context context, List<Message> userMessageList)
    {
        this.userMessageList = userMessageList;
        this.context=context;
    }

    private class MyChatViewHolder extends RecyclerView.ViewHolder {
        public TextView username;
        public ImageView profile_image, message_sender_image_view;
        public WebView displayView;
        public TextView show_message;
        public TextView sender_show_date;
        public LinearLayout message_box_right, displayViewLayout, message_sender_image_view_layout;
        public ProgressBar mProgressBar;
        public MyChatViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.imageView3);
            message_sender_image_view = itemView.findViewById(R.id.message_sender_image_view);
            message_sender_image_view_layout = itemView.findViewById(R.id.message_sender_image_view_layout);
            displayView = itemView.findViewById(R.id.displayView);
            displayViewLayout = itemView.findViewById(R.id.displayView_layout);
            message_box_right = itemView.findViewById(R.id.message_box_right);
            show_message = itemView.findViewById(R.id.show_message);
            sender_show_date = itemView.findViewById(R.id.sender_show_date);
            mProgressBar = itemView.findViewById(R.id.progress_circle);
        }
    }

    private class OtherChatViewHolder extends RecyclerView.ViewHolder {
        public TextView username;
        public ImageView profile_image, message_receiver_image_view;
        public WebView displayView;
        public TextView show_message;
        public TextView receiver_show_date;
        public  LinearLayout message_box_left;
        public RelativeLayout displayViewLayout, message_receiver_image_view_layout;
        public ProgressBar mProgressBar;

        public OtherChatViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.imageView3);
            message_receiver_image_view = itemView.findViewById(R.id.message_receiver_image_view);
            message_receiver_image_view_layout = itemView.findViewById(R.id.message_receiver_image_view_layout);
            displayView = itemView.findViewById(R.id.displayView);
            displayViewLayout = itemView.findViewById(R.id.displayView_layout);
            message_box_left = itemView.findViewById(R.id.message_box_left);
            show_message = itemView.findViewById(R.id.show_message);
            receiver_show_date = itemView.findViewById(R.id.receiver_show_date);
            mProgressBar = itemView.findViewById(R.id.progress_circle);
        }
    }

    // If the message is from current user, it will be displayed on the right,
    // else other users message will be on the left.
    @Override
    public int getItemViewType(int position) {

       if (TextUtils.equals(userMessageList.get(position).getFrom(),
               FirebaseAuth.getInstance().getCurrentUser().getUid()))
        {
            return MSG_TYPE_RIGHT;
        }
       else {
           return MSG_TYPE_LEFT;
       }
    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder viewHolder = null;
        firebaseAuth = FirebaseAuth.getInstance();
        playing=false;

        switch (viewType) {
           case MSG_TYPE_RIGHT:
               View viewChatMine = layoutInflater.inflate(R.layout.item_message_right, parent, false);
               viewHolder = new MyChatViewHolder(viewChatMine);
                break;
            case MSG_TYPE_LEFT:
                View viewChatOther = layoutInflater.inflate(R.layout.item_message_left, parent, false);
                viewHolder = new OtherChatViewHolder(viewChatOther);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

             if (TextUtils.equals(userMessageList.get(position).getFrom(),
                      FirebaseAuth.getInstance().getCurrentUser().getUid())){

                configureMyChatViewHolder((MyChatViewHolder) holder, position);

            } else {
                configureOtherChatViewHolder((OtherChatViewHolder) holder, position);

            }
        }

    // Display current user's messages on the right.
    private void configureMyChatViewHolder(final MyChatViewHolder myChatViewHolder, final int position) {
        final Message message = userMessageList.get(position);

        // Set date.
        Calendar calendar = Calendar.getInstance();
        Date date = new Date();
        int DAY_IN_MILLIS =  1000 * 60 * 60 * 24;
        /*int THREE_MONTHS_IN_MILLIS = DAY_IN_MILLIS;// 91 * DAY_IN_MILLIS;*/

        final SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");

        String CurrentDate = currentDate.format(calendar.getTime());
        String prevDate = currentDate.format(date.getTime() - DAY_IN_MILLIS);
       /* String afterThreeMonthsDate= currentDate.format(date.getTime() + THREE_MONTHS_IN_MILLIS );
        String CurrentTime = currentTime.format(calendar.getTime());
*/

        if (message.getDate().equals(CurrentDate)){
            myChatViewHolder.sender_show_date .setText(message.getTime());
        }
        else if(message.getDate().equals(prevDate)){
            myChatViewHolder.sender_show_date .setText("Yesterday");
        }
        else {
            myChatViewHolder.sender_show_date.setText(message.getDate());
        }

        // Set codename.

        int codename_length = message.getName().length();
        int space_count = 0;
        String space = " ";

        switch (codename_length) {
            case 1: space_count = 11;
                break;
            case 2: space_count = 10;
                break;
            case 3: space_count = 9;
                break;
            case 4: space_count = 8;
                break;
            case 5: space_count = 5;
                break;
            case 6: space_count = 4;
                break;
            case 7: space_count = 3;
                break;

        }

        // space_count is number of repetition of the string.
        // space is the name of the string.
        String spacing = new String(new char[space_count]).replace("\0",space);

        myChatViewHolder.username.setText( spacing + message.getName());


        // Set picture.
        try {
            imageReference = storageReference.child("Profile Picture").child(message.getFrom() + "/" + "profilepic.jpg");

            if (imageReference == null) { }
            else {
                imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        Picasso.get().load(uri).error(R.drawable.profile3).fit().centerCrop().into(myChatViewHolder.profile_image);

                    }
                });
            }

            }catch(Exception e){
                e.getLocalizedMessage();
            }

        // If codename is more than 9 characters, the chat blocks on item_message_right.xml
        // will be shifted to fit beside the length of the codename.

        ViewGroup.MarginLayoutParams marginParams1 = (ViewGroup.MarginLayoutParams)myChatViewHolder.show_message.getLayoutParams();
        ViewGroup.MarginLayoutParams marginParams2 = (ViewGroup.MarginLayoutParams)myChatViewHolder.displayViewLayout.getLayoutParams();
        ViewGroup.MarginLayoutParams marginParams3 = (ViewGroup.MarginLayoutParams)myChatViewHolder.message_sender_image_view_layout.getLayoutParams();
        ViewGroup.MarginLayoutParams marginParams4 = (ViewGroup.MarginLayoutParams)myChatViewHolder.profile_image.getLayoutParams();

        if (codename_length > 9){

            marginParams1.setMargins(0,2,170,0);
            marginParams2.setMargins(80,0,0,0);
            marginParams3.setMargins(240,0,0,0);
            marginParams4.setMargins(100,0,0,0);

        }

        // Set message.
        // Based on the message type, will show in different view.
        if(message.getType().equals("text")){
            myChatViewHolder.message_sender_image_view.setVisibility(View.INVISIBLE);
            myChatViewHolder.displayView.setVisibility(View.INVISIBLE);
            myChatViewHolder.mProgressBar.setVisibility(View.INVISIBLE);
            myChatViewHolder.message_box_right.setVisibility(View.VISIBLE);
            myChatViewHolder.show_message.setText(message.getMessage());
            myChatViewHolder.show_message.setTextIsSelectable(true);

            myChatViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

        }
        else if (message.getType().equals("gallery")||message.getType().equals("camera"))
        {
            myChatViewHolder.message_box_right.setVisibility(View.INVISIBLE);
            myChatViewHolder.displayView.setVisibility(View.INVISIBLE);
            myChatViewHolder.mProgressBar.setVisibility(View.INVISIBLE);
            myChatViewHolder.message_sender_image_view.setVisibility(View.VISIBLE);
            myChatViewHolder.message_sender_image_view.setBackgroundResource(R.drawable.background);
            Picasso.get().load(message.getMessage()).into(myChatViewHolder.message_sender_image_view);

           myChatViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // When the picture is clicked, userMessageList.get(position).getMessage()
                    // is sent as "filepath" to ZoomPicActivity.
                    // Then, ZoomPicActivity is loaded.

                    Intent intent = new Intent(myChatViewHolder.itemView.getContext(), ZoomPicActivitiy.class);
                    intent.putExtra("filepath",userMessageList.get(position).getMessage());

                    myChatViewHolder.itemView.getContext().startActivity(intent);

                }
            });


        }
        else if (message.getType().equals("pdf") || message.getType().equals("docx") ){
            myChatViewHolder.message_box_right.setVisibility(View.INVISIBLE);
            myChatViewHolder.displayView.setVisibility(View.INVISIBLE);
            myChatViewHolder.mProgressBar.setVisibility(View.INVISIBLE);
            myChatViewHolder.message_sender_image_view.setVisibility(View.VISIBLE);

            // Load file pic, instead of images from firebase.
            // For some reason, without Picasso, images from firebase will be loaded instead of ic_insert_file.
            Picasso.get().load(R.drawable.ic_insert_file).fit().centerCrop().into(myChatViewHolder.message_sender_image_view);
            myChatViewHolder.message_sender_image_view.setBackgroundResource(R.drawable.ic_insert_file);

            myChatViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {

                    CharSequence options[] = new CharSequence[]
                            {
                                    "Yes",
                                    "No"
                            };

                    AlertDialog.Builder builder = new AlertDialog.Builder(myChatViewHolder.itemView.getContext());
                    builder.setTitle("Do you want to download the file?");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (i == 0)
                            {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
                                myChatViewHolder.itemView.getContext().startActivity(intent);

                            }
                        }
                    });
                    builder.show();

                }
            });
        }
        else if (message.getType().equals("audio")){
            myChatViewHolder.message_sender_image_view.setVisibility(View.INVISIBLE);
            myChatViewHolder.displayView.setVisibility(View.INVISIBLE);
            myChatViewHolder.mProgressBar.setVisibility(View.VISIBLE);
            myChatViewHolder.message_box_right.setVisibility(View.VISIBLE);
            myChatViewHolder.show_message.setText("Listen to audio....");
            myChatViewHolder.mProgressBar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!playing) {
                        playing=true;
                        myChatViewHolder.show_message.setText("Listening to audio....");
                        myChatViewHolder.mProgressBar.setBackground(context.getDrawable(R.drawable.circle_shape));
                        myChatViewHolder.mProgressBar.setProgressDrawable(context.getDrawable(R.drawable.circular_progress_bar));
                        final MediaPlayer mediaPlayer = new MediaPlayer();
                        try {
                            mediaPlayer.setDataSource(userMessageList.get(position).getMessage());
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer player) {
                                    player.release();
                                    playing=false;
                                    myChatViewHolder.show_message.setText("Listen to audio....");
                                    myChatViewHolder.mProgressBar.setBackground(context.getDrawable(R.drawable.ic_play_circle));
                                    myChatViewHolder.mProgressBar.setProgressDrawable(context.getDrawable(R.drawable.empty));
                                }
                            });
                            myChatViewHolder.mProgressBar.setProgress(0);
                            final int duration = mediaPlayer.getDuration();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    while ( mediaPlayer.isPlaying()) {
                                        final int pStatus = (mediaPlayer.getCurrentPosition()*100) / duration;
                                        myChatViewHolder.mProgressBar.setProgress(pStatus);
                                    }
                                }
                            }).start();
                        } catch (Exception e) {
                        }
                    }
                }
            });
        }
        else if (message.getType().equals("gif"))
        {
            myChatViewHolder.message_box_right.setVisibility(View.INVISIBLE);
            myChatViewHolder.message_sender_image_view.setVisibility(View.INVISIBLE);
            myChatViewHolder.displayView.setVisibility(View.VISIBLE);
            myChatViewHolder.mProgressBar.setVisibility(View.INVISIBLE);
            myChatViewHolder.message_sender_image_view.setBackgroundResource(R.drawable.background);

            myChatViewHolder.displayView.loadUrl(message.getMessage());

        }


        // Delete message.
        myChatViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete",
                                        "Cancel"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(myChatViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0)
                                {
                                    deleteSentMessages(position, myChatViewHolder);
                                    getMessageIdType(position,myChatViewHolder);

                                }
                                else if (i == 1)
                                {
                                    // Nothing happens
                                }

                            }
                        });
                        builder.show();


                    return true;

            }});


        // Delete data which are 2 months older (around 61 days)
        myRef = FirebaseDatabase.getInstance().getReference();
        currentGroupName = userMessageList.get(position).getTo();

        DatabaseReference dataRef = myRef.child("Messages").child("Groups").child(currentGroupName).child("All Messages");

        dataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(final DataSnapshot data : dataSnapshot.getChildren())
                {
                    String date = data.child("date").getValue(String.class);

                    SimpleDateFormat originalFormat = new SimpleDateFormat("MMM dd, yyyy");
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");

                    Date messagedate;
                    try {
                       messagedate = originalFormat.parse(date);
                       String newdate = simpleDateFormat.format(messagedate);

                       Calendar calendar = Calendar.getInstance();
                       String currentdate = new SimpleDateFormat("dd.MM.yyyy").format(calendar.getTime());

                        long difference = -1;

                        try {
                            Date messageDate = simpleDateFormat.parse(newdate);
                            Date currentDate = simpleDateFormat.parse(currentdate);

                            difference = Math.abs(currentDate.getTime() - messageDate.getTime());
                            long differenceDates = difference / (24 * 60 * 60 * 1000);

                            if (differenceDates >= 61)
                            {
                                data.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if((data.child("type").getValue(String.class).equals("pdf")|| data.child("type").getValue(String.class).equals("docx")|| data.child("type").getValue(String.class).equals("audio")))
                                        {
                                            StorageReference storageReference = FirebaseStorage.getInstance().getReference("Files").child(currentGroupName);
                                            if (data.child("type").getValue(String.class).equals("pdf"))
                                            {
                                                storageReference.child(data.child("messageID").getValue(String.class) + ".pdf" ).delete();

                                            }
                                            else  if (data.child("type").getValue(String.class).equals("docx"))
                                            {
                                                storageReference.child(data.child("messageID").getValue(String.class) + ".docx").delete();
                                            }
                                            else  if (data.child("type").getValue(String.class).equals("audio"))
                                            {
                                                storageReference.child(data.child("messageID").getValue(String.class) + ".3gp").delete();
                                            }

                                        }
                                        else if (data.child("type").getValue(String.class).equals("gallery")||data.child("type").getValue(String.class).equals("camera"))
                                        {
                                            StorageReference storageReference = FirebaseStorage.getInstance().getReference("Images")
                                                    .child("Image Files").child(currentGroupName);
                                            storageReference.child(data.child("messageID").getValue(String.class) + ".jpg").delete();
                                        }
                                        else if (data.child("type").getValue(String.class).equals("gif"))
                                        {
                                            StorageReference storageReference = FirebaseStorage.getInstance().getReference("GIF Images").child(currentGroupName);
                                            storageReference.child(data.child("messageID").getValue(String.class) + ".gif").delete();
                                        }
                                    }
                                });

                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
               throw databaseError.toException();
            }
        });

    }

    // Display other users' messages on the left.
    private void configureOtherChatViewHolder(final OtherChatViewHolder otherChatViewHolder, final int position) {
        final Message message = userMessageList.get(position);

        // Set date.
        Calendar calendar = Calendar.getInstance();
        Date date = new Date();
        int DAY_IN_MILLIS =  1000 * 60 * 60 * 24;

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");

        String CurrentDate = currentDate.format(calendar.getTime());
        String prevDate = currentDate.format(date.getTime() - DAY_IN_MILLIS);
        String CurrentTime = currentTime.format(calendar.getTime());

        if (message.getDate().equals(CurrentDate)){
            otherChatViewHolder.receiver_show_date.setText(message.getTime());
        }else if(message.getDate().equals(prevDate))
        {
            otherChatViewHolder.receiver_show_date.setText("Yesterday");
        }
        else {
            otherChatViewHolder.receiver_show_date.setText(message.getDate());
        }

        // Set codename.
        int codename_length = message.getName().length();
        int space_count = 0;
        String space = " ";

        switch (codename_length) {
            case 1: space_count = 7;
                break;
            case 2: space_count = 6;
                break;
            case 3: space_count = 5;
                break;
            case 4: space_count = 4;
                break;
            case 5: space_count = 2;
                break;
            case 6: space_count = 1;
                break;
            case 7: space_count = 0;
                break;
        }

        // space_count is number of repetition of the string.
        // space is the name of the string.
        String spacing = new String(new char[space_count]).replace("\0",space);

        otherChatViewHolder.username.setText( spacing + message.getName());


        // If codename is more than 9 characters, the chat blocks on item_message_left.xml
        // will be shifted to fit beside the length of the codename.
        ViewGroup.MarginLayoutParams marginParams1 = (ViewGroup.MarginLayoutParams)otherChatViewHolder.show_message.getLayoutParams();
        ViewGroup.MarginLayoutParams marginParams2 = (ViewGroup.MarginLayoutParams)otherChatViewHolder.displayViewLayout.getLayoutParams();
        ViewGroup.MarginLayoutParams marginParams3 = (ViewGroup.MarginLayoutParams)otherChatViewHolder.message_receiver_image_view_layout.getLayoutParams();

        if (codename_length > 9){

            marginParams1.setMargins(190,2,0,0);
            marginParams2.setMargins(230,0,0,0);
            marginParams3.setMargins(230,0,0,0);

        }

        // Set picture.
        try {
            imageReference = storageReference.child("Profile Picture").child(message.getFrom() + "/" + "profilepic.jpg");

            if (imageReference == null){ }
            else{
                imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).error(R.drawable.profile3).fit().centerCrop().into(otherChatViewHolder.profile_image);

                    }
                });
            }

        }catch(Exception e){
            e.getLocalizedMessage();
        }


        // Set message.
        // Based on the message type, will show in different view.
        if(message.getType().equals("text")){

            otherChatViewHolder.message_receiver_image_view.setVisibility(View.INVISIBLE);
            otherChatViewHolder.displayView.setVisibility(View.INVISIBLE);
            otherChatViewHolder.mProgressBar.setVisibility(View.INVISIBLE);
            otherChatViewHolder.message_box_left.setVisibility(View.VISIBLE);
            otherChatViewHolder.show_message.setText(message.getMessage());
            otherChatViewHolder.show_message.setTextIsSelectable(true);
            
            otherChatViewHolder.show_message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

        }
         else if (message.getType().equals("gallery"))
        {
            otherChatViewHolder.message_box_left.setVisibility(View.INVISIBLE);
            otherChatViewHolder.displayView.setVisibility(View.INVISIBLE);
            otherChatViewHolder.mProgressBar.setVisibility(View.INVISIBLE);
            otherChatViewHolder.message_receiver_image_view.setVisibility(View.VISIBLE);
            otherChatViewHolder.message_receiver_image_view.setBackgroundResource(R.drawable.background);
            Picasso.get().load(message.getMessage()).fit().centerCrop().into(otherChatViewHolder.message_receiver_image_view);

            otherChatViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // When the picture is clicked, userMessageList.get(position).getMessage()is sent as "filepath" to ZoomPicActivity
                    // Then, ZoomPicActivity is loaded.

                    Intent intent = new Intent(otherChatViewHolder.itemView.getContext(), ZoomPicActivitiy.class);
                    intent.putExtra("filepath",userMessageList.get(position).getMessage());

                     otherChatViewHolder.itemView.getContext().startActivity(intent);

                }
            });


        }
         else if (message.getType().equals("pdf") || message.getType().equals("docx") ){
            otherChatViewHolder.message_box_left.setVisibility(View.INVISIBLE);
            otherChatViewHolder.displayView.setVisibility(View.INVISIBLE);
            otherChatViewHolder.mProgressBar.setVisibility(View.INVISIBLE);
            otherChatViewHolder.message_receiver_image_view.setVisibility(View.VISIBLE);

            // Load file pic, instead of images from firebase.
            // For some reason, without Picasso, images from firebase will be loaded instead of ic_insert_file.
            Picasso.get().load(R.drawable.ic_insert_file).fit().centerCrop().into(otherChatViewHolder.message_receiver_image_view);
            otherChatViewHolder.message_receiver_image_view.setBackgroundResource(R.drawable.ic_insert_file);

            otherChatViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {

                    CharSequence options[] = new CharSequence[]
                            {
                                    "Yes",
                                    "No"
                            };

                    AlertDialog.Builder builder = new AlertDialog.Builder(otherChatViewHolder.itemView.getContext());
                    builder.setTitle("Do you want to download the file?");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (i == 0)
                            {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
                                otherChatViewHolder.itemView.getContext().startActivity(intent);

                            }
                        }
                    });
                    builder.show();
                }
            });
        }
        else if (message.getType().equals("audio")){
            otherChatViewHolder.message_receiver_image_view.setVisibility(View.INVISIBLE);
            otherChatViewHolder.displayView.setVisibility(View.INVISIBLE);
            otherChatViewHolder.mProgressBar.setVisibility(View.VISIBLE);
            otherChatViewHolder.message_box_left.setVisibility(View.VISIBLE);
            otherChatViewHolder.show_message.setText("Listen to audio....");

            otherChatViewHolder.mProgressBar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!playing) {
                        playing=true;
                        otherChatViewHolder.show_message.setText("Listening to audio....");
                        otherChatViewHolder.mProgressBar.setBackground(context.getDrawable(R.drawable.circle_shape));
                        otherChatViewHolder.mProgressBar.setProgressDrawable(context.getDrawable(R.drawable.circular_progress_bar));

                        final MediaPlayer mediaPlayer = new MediaPlayer();
                        try {
                            mediaPlayer.setDataSource(userMessageList.get(position).getMessage());
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer player) {
                                    player.release();
                                    playing = false;
                                    otherChatViewHolder.show_message.setText("Listen to audio....");
                                    otherChatViewHolder.mProgressBar.setBackground(context.getDrawable(R.drawable.ic_play_circle));
                                    otherChatViewHolder.mProgressBar.setProgressDrawable(context.getDrawable(R.drawable.empty));
                                }
                            });
                            otherChatViewHolder.mProgressBar.setProgress(0);
                            final int duration = mediaPlayer.getDuration();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    while ( mediaPlayer.isPlaying()) {
                                        final int pStatus = (mediaPlayer.getCurrentPosition()*100) / duration;
                                        otherChatViewHolder.mProgressBar.setProgress(pStatus);
                                    }
                                }
                            }).start();
                        } catch (Exception e) {
                        }
                    }
                }
            });

        }
        else if (message.getType().equals("gif"))
        {

            otherChatViewHolder.message_box_left.setVisibility(View.INVISIBLE);
            otherChatViewHolder.message_receiver_image_view.setVisibility(View.INVISIBLE);
            otherChatViewHolder.displayView.setVisibility(View.VISIBLE);
            otherChatViewHolder.mProgressBar.setVisibility(View.INVISIBLE);
            otherChatViewHolder.message_receiver_image_view.setBackgroundResource(R.drawable.background);

            otherChatViewHolder.displayView.loadUrl(message.getMessage());

        }


        // Delete data which are 2 months older (around 61 days).
        myRef = FirebaseDatabase.getInstance().getReference();
        currentGroupName = userMessageList.get(position).getTo();

        DatabaseReference dataRef = myRef.child("Messages").child("Groups").child(currentGroupName).child("All Messages");

        dataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(final DataSnapshot data : dataSnapshot.getChildren())
                {
                    String date = data.child("date").getValue(String.class);

                    SimpleDateFormat originalFormat = new SimpleDateFormat("MMM dd, yyyy");
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");

                    Date messagedate;
                    try {
                        messagedate = originalFormat.parse(date);
                        String newdate = simpleDateFormat.format(messagedate);

                        Calendar calendar = Calendar.getInstance();
                        String currentdate = new SimpleDateFormat("dd.MM.yyyy").format(calendar.getTime());

                        long difference = -1;

                        try {
                            Date messageDate = simpleDateFormat.parse(newdate);
                            Date currentDate = simpleDateFormat.parse(currentdate);


                            difference = Math.abs(currentDate.getTime() - messageDate.getTime());
                            long differenceDates = difference / (24 * 60 * 60 * 1000);

                            if (differenceDates >= 61)
                            {
                                data.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if((data.child("type").getValue(String.class).equals("pdf")|| data.child("type").getValue(String.class).equals("docx")|| data.child("type").getValue(String.class).equals("audio")))
                                        {
                                            StorageReference storageReference = FirebaseStorage.getInstance().getReference("Files").child(currentGroupName);
                                            if (data.child("type").getValue(String.class).equals("pdf"))
                                            {
                                                storageReference.child(data.child("messageID").getValue(String.class) + ".pdf" ).delete();

                                            }
                                            else  if (data.child("type").getValue(String.class).equals("docx"))
                                            {
                                                storageReference.child(data.child("messageID").getValue(String.class) + ".docx").delete();
                                            }
                                            else  if (data.child("type").getValue(String.class).equals("audio"))
                                            {
                                                storageReference.child(data.child("messageID").getValue(String.class) + ".3gp").delete();
                                            }

                                        }
                                        else if (data.child("type").getValue(String.class).equals("gallery")||data.child("type").getValue(String.class).equals("camera"))
                                        {
                                            StorageReference storageReference = FirebaseStorage.getInstance().getReference("Images")
                                                    .child("Image Files").child(currentGroupName);
                                            storageReference.child(data.child("messageID").getValue(String.class) + ".jpg").delete();
                                        }
                                        else if (data.child("type").getValue(String.class).equals("gif"))
                                        {
                                            StorageReference storageReference = FirebaseStorage.getInstance().getReference("GIF Images").child(currentGroupName);
                                            storageReference.child(data.child("messageID").getValue(String.class) + ".gif").delete();
                                        }
                                    }
                                });
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                throw databaseError.toException();
            }
        });
    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }

    private void deleteSentMessages(final int position, final MyChatViewHolder holder)
    {
        currentGroupName = userMessageList.get(position).getTo();
        myRef = FirebaseDatabase.getInstance().getReference();

        myRef.child("Messages").child("Groups").child(currentGroupName).child("All Messages")
                .child(userMessageList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful()) { }
                else
                {
                    Toast.makeText(holder.itemView.getContext(),"Error Occured.",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void getMessageIdType(final int position, final MyChatViewHolder myChatViewHolder)
    {
        if(userMessageList.get(position).getType().equals("pdf")||userMessageList.get(position).getType().equals("docx")||userMessageList.get(position).getType().equals("audio"))
        {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("Files").child(currentGroupName);
            if (userMessageList.get(position).getType().equals("pdf"))
            {
                storageReference.child(userMessageList.get(position).getMessageID() + ".pdf" ).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){}
                        else{Toast.makeText(myChatViewHolder.itemView.getContext(),"Error Occured.",Toast.LENGTH_SHORT).show();}
                    }

                });

            }
            else  if (userMessageList.get(position).getType().equals("docx"))
            {
                storageReference.child(userMessageList.get(position).getMessageID() + ".docx").delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){}
                        else{Toast.makeText(myChatViewHolder.itemView.getContext(),"Error Occured.",Toast.LENGTH_SHORT).show();}
                    }
                });
            }
            else  if (userMessageList.get(position).getType().equals("audio"))
            {
                storageReference.child(userMessageList.get(position).getMessageID() + ".3gp").delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){}
                        else{Toast.makeText(myChatViewHolder.itemView.getContext(),"Error Occured.",Toast.LENGTH_SHORT).show();}
                    }
                });
            }

        }
        else if (userMessageList.get(position).getType().equals("gallery")||userMessageList.get(position).getType().equals("camera"))
        {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("Images")
                    .child("Image Files").child(currentGroupName);
            storageReference.child(userMessageList.get(position).getMessageID() + ".jpg").delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){}
                    else{Toast.makeText(myChatViewHolder.itemView.getContext(),"Error Occured.",Toast.LENGTH_SHORT).show();}
                }
            });
        }
        else if (userMessageList.get(position).getType().equals("gif"))
        {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("GIF Images").child(currentGroupName);
            storageReference.child(userMessageList.get(position).getMessageID() + ".gif").delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){}
                    else{Toast.makeText(myChatViewHolder.itemView.getContext(),"Error Occured.",Toast.LENGTH_SHORT).show();}
                }
            });
        }
     }
    }
