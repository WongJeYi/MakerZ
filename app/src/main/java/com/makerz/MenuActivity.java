package com.makerz;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makerz.adapter.MenuAdapter;
import com.makerz.model.Data.MySingleton;
import com.makerz.model.FBData;
import com.makerz.model.MakerEvent;
import com.makerz.util.DatabaseHelper;
import com.makerz.util.DownloadImageTask;
import com.makerz.util.FirebaseUtil;
import com.makerz.util.OnTaskCompleted;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;

public class MenuActivity extends AppCompatActivity {

    private String userId, fullname, codename;
    private String[] retImage = {"default_image"};

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference myRef;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser currentUser;

    private ImageView Profile;
    private ImageView Exit;
    private FloatingActionButton Attendance;
    private ImageView Menu;
    private ImageView Activity;
    private ImageView Chat;
    private ImageView List;
    private ImageView MakerZ;

    // facebook page album
    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;

    // recyclerview for image display
    private RecyclerView tripRecylcerView;
    private RecyclerView contestRecylcerView;
    private RecyclerView activityRecylcerView;
    private MenuAdapter tripAdapter;
    private MenuAdapter contestAdapter;
    private MenuAdapter activityAdapter;

    private ArrayList<FBData> mFBData;


    // list of maker events
    private ArrayList<MakerEvent> trip = new ArrayList<>();
    private ArrayList<MakerEvent> activity = new ArrayList<>();
    private ArrayList<MakerEvent> contest = new ArrayList<>();


    // refresh pull down
    private SwipeRefreshLayout pullToRefresh;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        setupUIViews();

        // Get current user's info.
        firebaseAuth = FirebaseAuth.getInstance();
        pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // get facebook images
                getPost();
                pullToRefresh.setRefreshing(false);
            }
        });
        if (firebaseAuth.getCurrentUser() != null) {

            currentUser = firebaseAuth.getCurrentUser();
            userId = firebaseAuth.getCurrentUser().getUid();

            firebaseDatabase = FirebaseDatabase.getInstance();
            myRef = FirebaseDatabase.getInstance().getReference();

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

                    Chat.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent groupChatIntent = new Intent(MenuActivity.this, ChatActivity.class);
                            groupChatIntent.putExtra("visit_user_id", userId);
                            groupChatIntent.putExtra("visit_user_name", codename);
                            groupChatIntent.putExtra("visit_image", retImage[0]);
                            startActivity(groupChatIntent);
                            finish();
                        }
                    });

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


        Exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this, R.style.AlertDialog);
                builder.setTitle("Do you want to quit and logout?");
                builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(MenuActivity.this, "Logout successfully.", Toast.LENGTH_SHORT).show();
                        firebaseAuth.signOut();
                        MySingleton.deleteCache(MenuActivity.this);
                        ((ActivityManager) MenuActivity.this.getSystemService(ACTIVITY_SERVICE))
                                .clearApplicationUserData();
                        finish();
                        startActivity(new Intent(MenuActivity.this, MainActivity.class));

                    }
                });
                builder.show();
            }
        });

        MakerZ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(MenuActivity.this, MakerZActivity.class));

            }
        });

        Profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MenuActivity.this, ProfileActivity.class));
            }
        });

        Activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MenuActivity.this, ContentstuffActivity.class));
            }
        });

        List.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MenuActivity.this, ListMakerZActivity.class));
            }
        });

        Attendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MenuActivity.this, Attendance.class));
            }
        });

        //attendance use fragment activity
        SharedPreferences spref = PreferenceManager.getDefaultSharedPreferences(this);

// Creating an Editor object to edit(write to the file)
        SharedPreferences.Editor myEdit = spref.edit();

        if (spref.getBoolean("FirstTimeMenu", true)) {
            myEdit.putBoolean("FirstTimeMenu", false);
            myEdit.commit();
            newUser();
        }else if(spref.getBoolean("FirstTimeMenuA",true)){
            myEdit.putBoolean("FirstTimeMenuA", false);
            myEdit.commit();
            newUserA();
        }
    }

    private void newUserA() {
        TapTargetView.showFor(this, TapTarget.forView(Activity, "This is activity button", "You will be directed back to the activity page when this button is pressed")
                .outerCircleColor(R.color.colorPrimary)
                .outerCircleAlpha(0.96f)
                .targetCircleColor(R.color.browser_actions_bg_grey)
                .titleTextColor(R.color.colorPrimaryDark)
                .titleTextSize(30)
                .descriptionTextSize(20)
                .descriptionTextColor(R.color.colorPrimaryDark)
                .textTypeface(Typeface.SANS_SERIF)
                .drawShadow(true)
                .cancelable(false)
                .tintTarget(true)
                .dimColor(R.color.colorPrimary)
                .targetRadius(60)
                .transparentTarget(true), new TapTargetView.Listener() {
            @Override
            public void onTargetClick(TapTargetView view) {
                super.onTargetClick(view);
                view.dismiss(true);
                startActivity(new Intent(MenuActivity.this,ContentstuffActivity.class));
                finish();
            }
        });
    }

    private void newUser() {
        TapTargetView.showFor(this, TapTarget.forView(Menu, "This is home button", "You will be directed back here when this button is pressed")
                .outerCircleColor(R.color.colorPrimary)
                .outerCircleAlpha(0.96f)
                .targetCircleColor(R.color.browser_actions_bg_grey)
                .titleTextColor(R.color.colorPrimaryDark)
                .titleTextSize(30)
                .descriptionTextSize(20)
                .descriptionTextColor(R.color.colorPrimaryDark)
                .textTypeface(Typeface.SANS_SERIF)
                .drawShadow(true)
                .cancelable(false)
                .tintTarget(true)
                .dimColor(R.color.colorPrimary)
                .targetRadius(60)
                .transparentTarget(true), new TapTargetView.Listener() {
            @Override
            public void onTargetClick(TapTargetView view) {
                super.onTargetClick(view);
                view.dismiss(true);
                TapTargetView.showFor(MenuActivity.this, TapTarget.forView(Profile, "This is Profile button", "You can edit your profile here")
                        .outerCircleColor(R.color.colorPrimary)
                        .outerCircleAlpha(0.96f)
                        .targetCircleColor(R.color.browser_actions_bg_grey)
                        .titleTextColor(R.color.colorPrimaryDark)
                        .titleTextSize(30)
                        .descriptionTextSize(20)
                        .descriptionTextColor(R.color.colorPrimaryDark)
                        .textTypeface(Typeface.SANS_SERIF)
                        .drawShadow(true)
                        .cancelable(false)
                        .tintTarget(true)
                        .dimColor(R.color.colorPrimary)
                        .targetRadius(60)
                        .transparentTarget(true), new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        view.dismiss(true);
                        startActivity(new Intent(MenuActivity.this,ProfileActivity.class));
                        finish();
                    }
                });
            }
        });


    }


    @Override
    protected void onStart() {
        // clear list
        trip.clear();
        contest.clear();
        activity.clear();
        getEvent();

        // adapter for each recyclerview
        tripAdapter = new MenuAdapter(this, trip);
        activityAdapter = new MenuAdapter(this, activity);
        contestAdapter = new MenuAdapter(this, contest);
        // set adapter to recyclerview
        tripRecylcerView.setAdapter(tripAdapter);
        activityRecylcerView.setAdapter(activityAdapter);
        contestRecylcerView.setAdapter(contestAdapter);
        super.onStart();
    }

    @Override
    protected void onResume() {
        // clear list
        trip.clear();
        contest.clear();
        activity.clear();
        getEvent();

        // adapter for each recyclerview
        tripAdapter = new MenuAdapter(this, trip);
        activityAdapter = new MenuAdapter(this, activity);
        contestAdapter = new MenuAdapter(this, contest);
        // set adapter to recyclerview
        tripRecylcerView.setAdapter(tripAdapter);
        activityRecylcerView.setAdapter(activityAdapter);
        contestRecylcerView.setAdapter(contestAdapter);
        super.onResume();
    }

    private void setupUIViews() {
        Profile = findViewById(R.id.profile);
        Exit = findViewById(R.id.exit);
        Attendance = findViewById(R.id.attendance);
        Menu = findViewById(R.id.menu);
        Activity = findViewById(R.id.activity);
        Chat = findViewById(R.id.chat);
        List = findViewById(R.id.list);
        MakerZ = findViewById(R.id.makerz);
        mViewPager = findViewById(R.id.view_1);
        mFBData = new DatabaseHelper(this).getAllPosts();
        new DatabaseHelper(this).delete(this);
        if (mFBData.isEmpty()) {
            getPost();
        }
        mViewPagerAdapter = new ViewPagerAdapter(this, mFBData, true);
        mViewPager.setAdapter(mViewPagerAdapter);
        //set ontouch listener to intercept the call to swipe refresh layout
        mViewPager.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                final int X = (int) motionEvent.getRawX();
                final int Y = (int) motionEvent.getRawY();
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_MOVE:
                        pullToRefresh.setEnabled(false);
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        pullToRefresh.setEnabled(true);
                        break;

                }
                return false;
            }
        });
        tripRecylcerView = findViewById(R.id.view_2);
        activityRecylcerView = findViewById(R.id.view_3);
        contestRecylcerView = findViewById(R.id.view_4);
        // set horizontal layout manager to recyclerview
        tripRecylcerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        activityRecylcerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        contestRecylcerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));


    }

    private class ViewPagerAdapter extends PagerAdapter {

        private Context context;
        private LayoutInflater layoutInflater;
        private ArrayList<FBData> mFBData;
        private boolean offline;

        public ViewPagerAdapter(Context context, ArrayList<FBData> mFBData, boolean offline) {
            this.context = context;
            this.offline = offline;
            this.mFBData = mFBData;
        }


        @Override
        public int getCount() {
            return mFBData.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {

            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.image_layout, null);
            // set image to imageView
            final ImageView imageView = (ImageView) view.findViewById(R.id.post_image);
            final EditText textView = view.findViewById(R.id.post_message);
            // set animation to edittext
            // !!! the reason of using edittext is that textview will not show full text
            textView.startAnimation((Animation) AnimationUtils.loadAnimation(context, R.anim.horizontal_animation));
            final String image = mFBData.get(position).getFBImage();
            final String message = mFBData.get(position).getFBMessage();
            final String createdTime = mFBData.get(position).getFBCreatedTime();
            final String id = mFBData.get(position).getFBId();


            if (offline) {
                textView.setText(message);
                byte[] imageByteArray = Base64.decode(image, Base64.DEFAULT);
                Glide.with(context)
                        .load(imageByteArray)
                        .into(imageView);
            } else {
                textView.setText(message);
                Log.d("Message,H", message);
                new DownloadImageTask(imageView, new OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted(Bitmap bitmap, String encodedImage) {

                        // save to database
                        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
                        databaseHelper.insertPost(encodedImage, message, id, createdTime);
                    }
                }).execute(image);


            }

            ViewPager vp = (ViewPager) container;
            vp.addView(view, 0);
            return view;

        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            ViewPager vp = (ViewPager) container;
            View view = (View) object;
            vp.removeView(view);

        }
    }


    private void getPost() {

        Bundle parameters = new Bundle();
        parameters.putString("fields", "feed{full_picture,message,created_time}");
        AccessToken accessToken = new AccessToken(getString(R.string.facebook_access_token), getString(R.string.facebook_app_id), getString(R.string.facebook_user_id), Arrays.asList("pages_read_engagement"), null, null, null, null, null, null);

        GraphRequest request = new GraphRequest(
                accessToken,
                "/2092483070983335",
                parameters,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        try {
                            // These are following the data structure from facebook graph response
                            if (response.getError() == null) {
                                JSONObject joMain = response.getJSONObject(); //convert GraphResponse response to JSONObject
                                if (joMain.has("feed")) {
                                    Log.d("Hellos", response.toString());
                                    JSONArray jaData = joMain.optJSONObject("feed").optJSONArray("data");//find JSONArray from JSONObject
                                    mFBData.clear();// clear mFBData
                                    for (int i = 0; i < jaData.length(); i++) {//find no. of post using jaData.length()

                                        JSONObject joPost = jaData.getJSONObject(i); //get each post as JSONObject
                                        String message = joPost.optString("message"); //find message from post object
                                        String id = joPost.optString("id");//find id from post object
                                        String createdTime = joPost.optString("created_time");//find created_time from post object

                                        String formdata = joPost.getString("full_picture");//find full_picture from post object
                                        String url = null;
                                        try {
                                            url = URLDecoder.decode(formdata, StandardCharsets.UTF_8.toString());
                                        } catch (UnsupportedEncodingException e) {
                                            e.printStackTrace();
                                        }

                                        mFBData.add(new FBData(url, message, id, createdTime));
                                        Log.v("PostId, message", id + " " + message);
                                        Log.v("URL", url);
                                        Log.v("Hello", joPost.optString("id"));
                                    }
                                    Collections.reverse(mFBData);
                                    mViewPagerAdapter = new ViewPagerAdapter(getApplicationContext(), mFBData, false);
                                    mViewPager.setAdapter(mViewPagerAdapter);
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Busy", Toast.LENGTH_LONG).show();
                                Log.d("Test", response.getError().toString());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        request.executeAsync();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    // getEvent gets the event lists from Firebase
    private void getEvent() {
        // get database
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            // get reference
            DatabaseReference databaseReference = firebaseDatabase.getReference().child("Events");
            ValueEventListener postListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d("Count ", "" + dataSnapshot.getChildrenCount());
                    if (dataSnapshot.getValue() == null) {

                    } else {
                        trip.clear();
                        activity.clear();
                        contest.clear();
                        MakerEvent makerEvent;

                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            makerEvent = postSnapshot.getValue(MakerEvent.class);
                            if (makerEvent.isVerified() || MySingleton.isAdmin) {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                Date date = null;
                                Date today = new Date();
                                Calendar c = Calendar.getInstance();
                                c.setTime(today);
                                c.add(Calendar.DATE, -1);
                                today = c.getTime();
                                try {
                                    date = sdf.parse(makerEvent.getDeadline());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    if (Objects.isNull(date)) {
                                        Log.d("DEl", makerEvent.getTitle());
                                        postSnapshot.getRef().removeValue();
                                    }
                                }
                                if (date.before(today)) {
                                    Log.d("DEl", makerEvent.getTitle());
                                    postSnapshot.getRef().removeValue();
                                } else {
                                    Log.d("Get Data", makerEvent.getTitle() + " ");
                                    String thumbnail = makerEvent.getThumbnail();
                                    if (makerEvent.getType().equals("Trip")) {
                                        trip.add(makerEvent);
                                        Log.d("TRIP", thumbnail + " ");
                                    } else if (makerEvent.getType().equals("Activity")) {
                                        activity.add(makerEvent);
                                        Log.d("ACTIVITY", thumbnail + " ");
                                    } else if (makerEvent.getType().equals("Contest")) {
                                        contest.add(makerEvent);
                                        Log.d("CONTEST", thumbnail + " ");
                                    }
                                }
                            }

                        }

                        tripAdapter.notifyDataSetChanged();
                        activityAdapter.notifyDataSetChanged();
                        contestAdapter.notifyDataSetChanged();
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    Log.w("How", "loadPost:onCancelled", databaseError.toException());
                    // ...
                }
            };
            // listen for one time only
            databaseReference.addListenerForSingleValueEvent(postListener);
        } else {
            Toast.makeText(this, "No connection", Toast.LENGTH_SHORT).show();
        }
    }

    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this, R.style.AlertDialog);
        builder.setTitle("Do you want to quit?");
        builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.show();
    }

}

