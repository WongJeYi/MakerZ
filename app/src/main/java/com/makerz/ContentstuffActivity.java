package com.makerz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.material.tabs.TabLayout;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makerz.model.Data.MySingleton;

public class ContentstuffActivity extends AppCompatActivity implements EventFragment.OnFragmentInteractionListener {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contentstuff);

        setupUIViews();

        // Get current user's info.
        firebaseAuth = FirebaseAuth.getInstance();

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
                            Intent groupChatIntent = new Intent(ContentstuffActivity.this, ChatActivity.class);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(ContentstuffActivity.this, R.style.AlertDialog);
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
                        Toast.makeText(ContentstuffActivity.this, "Logout successfully.", Toast.LENGTH_SHORT).show();
                        firebaseAuth.signOut();
                        finish();
                        startActivity(new Intent(ContentstuffActivity.this, MainActivity.class));

                    }
                });
                builder.show();
            }
        });

        MakerZ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ContentstuffActivity.this, MakerZActivity.class));
                finish();

            }
        });

        Profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ContentstuffActivity.this, ProfileActivity.class));
                finish();
            }
        });

        Menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        List.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ContentstuffActivity.this, ListMakerZActivity.class));
                finish();
            }
        });

        Attendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ContentstuffActivity.this, Attendance.class));
                finish();

            }
        });

        //attendance use fragment activity
        SharedPreferences spref = PreferenceManager.getDefaultSharedPreferences(this);

        // Creating an Editor object to edit(write to the file)
        SharedPreferences.Editor myEdit = spref.edit();

        if (spref.getBoolean("FirstTimeContentstuff", true)) {
            myEdit.putBoolean("FirstTimeContentstuff", false);
            myEdit.commit();
            TapTargetView.showFor(ContentstuffActivity.this, TapTarget.forView(Attendance, "This is the attendance button", "You can update your attendance by clicking here")
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
                    startActivity(new Intent(ContentstuffActivity.this,Attendance.class));
                    finish();
                }
            });
        }else if (spref.getBoolean("FirstTimeContentstuffA", true)) {
            myEdit.putBoolean("FirstTimeContentstuffA", false);
            myEdit.commit();
            TapTargetView.showFor(ContentstuffActivity.this, TapTarget.forView(Chat, "This is the Chat button", "You can chat with MakerZ members by clicking here")
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
                    Intent groupChatIntent = new Intent(ContentstuffActivity.this, ChatActivity.class);
                    groupChatIntent.putExtra("visit_user_id", userId);
                    groupChatIntent.putExtra("visit_user_name", codename);
                    groupChatIntent.putExtra("visit_image", retImage[0]);
                    startActivity(groupChatIntent);
                    finish();
                }
            });
        }
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
        // initiate tabs and paging
        paging(tab());
        /*
        Activity_button = findViewById(R.id.button_activity);
        Contest_button= findViewById(R.id.button_contest);
        Trip_button = findViewById(R.id.button_trip);

         */
    }

    private TabLayout tab() {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Trips"));
        tabLayout.addTab(tabLayout.newTab().setText("Activities"));
        tabLayout.addTab(tabLayout.newTab().setText("Contests"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        return tabLayout;
    }

    private void paging(TabLayout tabLayout) {
        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter pagerAdapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        // each page will have one event fragment
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // set page
                viewPager.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // set page
                viewPager.setCurrentItem(tab.getPosition());

            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private class PagerAdapter extends FragmentStatePagerAdapter {
        int mNumOfTabs;

        public PagerAdapter(FragmentManager fm, int NumOfTabs) {
            super(fm);
            this.mNumOfTabs = NumOfTabs;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {

                case 0:
                    EventFragment trips = EventFragment.newInstance("Trip", "");
                    return trips;
                case 1:
                    EventFragment activities = EventFragment.newInstance("Activity", "");
                    return activities;
                case 2:
                    EventFragment contests = EventFragment.newInstance("Contest", "");
                    return contests;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return mNumOfTabs;
        }
    }
}
