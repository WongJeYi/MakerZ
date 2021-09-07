package com.makerz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makerz.adapter.AttendanceAdapter;
import com.makerz.model.Data.MySingleton;
import com.makerz.model.MakerEvent;
import com.makerz.util.EventDecorator;
import com.makerz.util.EventDot;
import com.makerz.util.FirebaseUtil;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Attendance extends AppCompatActivity {
    // calendarView is the calendar in UI
    private MaterialCalendarView calendarView;
    // calevents is the event list in CalendarDay
    private List<CalendarDay> calevents = new ArrayList<>();
    // calchecks is the attendance list in CalendarDay
    private List<CalendarDay> calchecks = new ArrayList<>();

    // listView shows event name of selected dates
    private ListView listView;
    private ArrayList<MakerEvent> eventList = new ArrayList<>();
    // HashMap for addEventDecorator
    private HashMap<Integer, List<MakerEvent>> map = new HashMap<>();
    private AttendanceAdapter adapter;

    private Calendar cal;
    // buttons to add and delete events
    private FloatingActionButton mFloatingActionButton, mFloatingActionButtonDelete;
    // clickedDelete to check mFloatingActionButtonDelete is clicked
    private boolean clickedDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // check permission
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        clickedDelete = false;
        // setup listview
        listView = (ListView) findViewById(R.id.list_event);
        adapter = new AttendanceAdapter(this, eventList);
        listView.setAdapter(adapter);
        // setup mFloatingActionButton
        mFloatingActionButton = findViewById(R.id.floating_button);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Attendance.this, AddNoteActivity.class));

            }
        });

        // setup calendarView
        calendarView = findViewById(R.id.attendance_calendar);
        // the background color of the calendar
        calendarView.setBackgroundColor(Color.BLACK);
        // set current date
        final Calendar calendar = Calendar.getInstance();
        calendarView.setSelectedDate(calendar.getTime());
        // handle on date change
        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                calendarView.setHeaderTextAppearance(R.style.AppTheme);
                if (clickedDelete) {
                    //show delete dialog
                    deleteDialog(date);
                }
                // refresh list of events in listview
                Log.d("ADD", String.valueOf(eventList.size()));
                // clone eventList to list
                ArrayList<MakerEvent> list = (ArrayList<MakerEvent>) eventList.clone();
                // clear eventList
                adapter.clearData();
                for (MakerEvent event : list) {

                    Log.d("ADD", String.valueOf(eventList.size()) + "sdsd");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                    Date deadline = null;
                    try {
                        deadline = sdf.parse(event.getDeadline());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Date Selected = date.getDate();

                    Log.d("ADD", event.getTitle() + event.getDeadline() + " DAte:" + Selected.toString());

                    if (deadline.equals(Selected)) {
                        adapter.add(event);

                    }
                }
                // return list to eventList
                eventList = list;
                adapter.notifyDataSetChanged();
            }
        });
        // setup mFloatingActionButtonDelete button
        mFloatingActionButtonDelete = findViewById(R.id.floating_button_delete);
        mFloatingActionButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendarView.setSelectedDate((CalendarDay) null);
                Toast.makeText(Attendance.this, "Select the event date to delete", Toast.LENGTH_LONG).show();
                clickedDelete = true;

            }
        });
        SharedPreferences spref = PreferenceManager.getDefaultSharedPreferences(this);

        // Creating an Editor object to edit(write to the file)
        SharedPreferences.Editor myEdit = spref.edit();

        if (spref.getBoolean("FirstTimeAttendance", true)) {
            myEdit.putBoolean("FirstTimeAttendance", false);
            myEdit.commit();
            TapTargetView.showFor(Attendance.this, TapTarget.forView(mFloatingActionButton, "This is the add event button", "You can suggest new events by clicking here")
                    .outerCircleColor(R.color.colorPrimaryDark)
                    .outerCircleAlpha(0.96f)
                    .targetCircleColor(R.color.browser_actions_bg_grey)
                    .titleTextColor(R.color.colorPrimary)
                    .titleTextSize(30)
                    .descriptionTextSize(20)
                    .descriptionTextColor(R.color.colorPrimary)
                    .textTypeface(Typeface.SANS_SERIF)
                    .drawShadow(true)
                    .cancelable(false)
                    .tintTarget(true)
                    .dimColor(R.color.colorPrimaryDark)
                    .targetRadius(60)
                    .transparentTarget(true), new TapTargetView.Listener() {
                @Override
                public void onTargetClick(TapTargetView view) {
                    super.onTargetClick(view);
                    view.dismiss(true);
                    startActivity(new Intent(Attendance.this,AddNoteActivity.class));
                    finish();
                }
            });
        }else if (spref.getBoolean("FirstTimeAttendanceA", true)) {
            myEdit.putBoolean("FirstTimeAttendanceA", false);
            myEdit.commit();
            TapTargetView.showFor(Attendance.this, TapTarget.forView(mFloatingActionButtonDelete, "This is the delete event button", "You can delete your events by clicking here")
                    .outerCircleColor(R.color.colorPrimaryDark)
                    .outerCircleAlpha(0.96f)
                    .targetCircleColor(R.color.browser_actions_bg_grey)
                    .titleTextColor(R.color.colorPrimary)
                    .titleTextSize(30)
                    .descriptionTextSize(20)
                    .descriptionTextColor(R.color.colorPrimary)
                    .textTypeface(Typeface.SANS_SERIF)
                    .drawShadow(true)
                    .cancelable(false)
                    .tintTarget(true)
                    .dimColor(R.color.colorPrimaryDark)
                    .targetRadius(60)
                    .transparentTarget(true), new TapTargetView.Listener() {
                @Override
                public void onTargetClick(TapTargetView view) {
                    super.onTargetClick(view);
                    view.dismiss(true);
                    startActivity(new Intent(Attendance.this,ContentstuffActivity.class));
                    finish();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        calchecks.clear();
        eventList.clear();
        updateAttendance();
        getEvent();
        getAttendanceOfMonth();
    }

    private void deleteDialog(@NonNull CalendarDay date) {
        // arrayAdapter is the adapter with list of events on selected date
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.select_dialog_singlechoice);
        for (MakerEvent event : eventList) {
            // get deadline in format
            Log.d("ADD", String.valueOf(eventList.size()) + "sdsd");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Date deadline = null;
            try {
                deadline = sdf.parse(event.getDeadline());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date Selected = date.getDate();

            Log.d("ADD", event.getTitle() + event.getDeadline() + " DAte:" + Selected.toString());
            // get lists of events on selected date
            if (deadline.equals(Selected)) {
                arrayAdapter.add(event.getTitle());

            }
        }
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(Attendance.this, R.style.DialogTheme);
        builderSingle.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.trashcan));
        builderSingle.setTitle("Select one to delete:-");
        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                clickedDelete = false;
            }
        });
        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String strName = arrayAdapter.getItem(which);
                final AlertDialog.Builder builderInner = new AlertDialog.Builder(Attendance.this, R.style.DialogTheme);
                builderInner.setMessage(strName);
                builderInner.setTitle("Are you sure to delete?");
                final EditText input = new EditText(Attendance.this);
                // check event ownership by using password, also to confirm delete
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                input.setHint("Heriot-Watt Email Address");
                builderInner.setView(input);
                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if ((input.getText().toString()).matches("")) {
                            input.setError("Please input value");
                        } else {
                            // delete the event with Eventname and password
                            deleteEvent(strName, input.getText().toString());
                            dialog.dismiss();
                            clickedDelete = false;
                        }
                    }
                });
                builderInner.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        clickedDelete = false;
                    }
                });
                builderInner.show();
            }
        });
        builderSingle.show();
    }

    private void deleteEvent(final String eventTitle, final String input) {
        // get Database
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            // database reference for events
            final DatabaseReference databaseReference = firebaseDatabase.getReference().child("Events").child(eventTitle);
            ValueEventListener postListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String email = (String) userSnapshot.getValue();

                        if (email.equals(input)) {
                            //delete and refresh
                            databaseReference.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            Toast.makeText(Attendance.this, "Successfully delete : " + eventTitle, Toast.LENGTH_LONG).show();
                                            finish();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                }
                            });


                            break;
                        }

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            databaseReference.addListenerForSingleValueEvent(postListener);
        }
    }

    private void getAttendanceOfMonth() {
        // get database
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();


        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            // get reference under Attendance, userId
            final DatabaseReference databaseReference = firebaseDatabase.getReference().child("Attendance").child(uid);
            ValueEventListener postListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    Log.d("Count ", "" + dataSnapshot.getChildrenCount());
                    ArrayList<String> checked = new ArrayList<>();
                    if (dataSnapshot.getValue() == null) {

                    } else {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                            String day = (String) postSnapshot.getKey();
                            // get date in format
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            Date date = null;
                            Calendar dateInCalendar = null;
                            Date today = new Date();
                            cal = Calendar.getInstance();
                            try {
                                date = sdf.parse(day);
                                dateInCalendar = Calendar.getInstance();
                                cal.setTime(date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            Calendar twoMonthsAgo = Calendar.getInstance();
                            twoMonthsAgo.add(Calendar.MONTH, -2);
                            // delete attendance 2 months before current month

                            if (dateInCalendar.before(twoMonthsAgo) && dateInCalendar != null) {
                                postSnapshot.getRef().removeValue();
                            } else {
                                Log.d("Get Data", day);
                                checked.add(day);
                                CalendarDay calendarDay = CalendarDay.from(date);
                                cal = Calendar.getInstance();
                                cal.setTime(date);
                                // add to calchecks
                                calchecks.add(calendarDay);
                                Log.d("Calevent", calendarDay.toString());
                            }

                        }
                        if (calchecks != null && calchecks.size() > 0) {
                            // add the tick to attendance
                            Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.tick);
                            EventDecorator eventDecorator = new EventDecorator(drawable, calchecks);
                            calendarView.addDecorator(eventDecorator);
                        }
                    }

                }


                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    Log.w("How", "loadPost:onCancelled", databaseError.toException());
                    // ...
                }
            };
            databaseReference.addListenerForSingleValueEvent(postListener);
        } else {
            Toast.makeText(this, "No connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void getEvent() {
        // get database
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            // get events reference
            DatabaseReference databaseReference = firebaseDatabase.getReference().child("Events");
            ValueEventListener postListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d("Count ", "" + dataSnapshot.getChildrenCount());
                    if (dataSnapshot.getValue() == null) {

                    } else {
                        // loop for all event under Events
                        eventList.clear();
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                            MakerEvent makerEvent = postSnapshot.getValue(MakerEvent.class);
                            if (MySingleton.isAdmin || makerEvent.isVerified()) {
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
                                // delete if it is yesterday
                                if (date.before(today)) {
                                    Log.d("DEl", makerEvent.getTitle());
                                    postSnapshot.getRef().removeValue();
                                    postSnapshot.getRef().addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                } else {
                                    Log.d("Get Data", makerEvent.getTitle());
                                    eventList.add(makerEvent);
                                    CalendarDay day = CalendarDay.from(date);
                                    cal = Calendar.getInstance();
                                    cal.setTime(date);
                                    calevents.add(day);
                                    Log.d("Calevent", day.toString());

                                }

                                adapter.notifyDataSetChanged();
                            }
                            if (!eventList.equals(null) && eventList.size() > 0) {
                                // add red dot decorator to days with events
                                map.put(cal.get(Calendar.MONTH), eventList);
                                DotSpan dotSpan = new DotSpan(5, Color.RED);
                                EventDot eventDot = new EventDot(dotSpan, calevents, getApplicationContext());
                                calendarView.addDecorator(eventDot);
                            }
                        }
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    Log.w("How", "loadPost:onCancelled", databaseError.toException());
                    // ...
                }
            };
            databaseReference.addListenerForSingleValueEvent(postListener);
        } else {
            Toast.makeText(this, "No connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateAttendance() {
        // get database
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();


        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            // get reference

            final Calendar calendar = Calendar.getInstance();
            Date today = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            final String day = sdf.format(today);
            final DatabaseReference postRef = firebaseDatabase.getReference().child("Attendance").child(uid).child(String.valueOf(day));
            postRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                    // add to attendance google sheet
                    //addItemToSheet(name,day);
                    if (!dataSnapshot.exists()) {
                        //do something if not exists

                        Log.d("Message", String.valueOf(day));
                        // set value to Attendance.uid.day
                        postRef.setValue("Yes");

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } else {
            Toast.makeText(this, "No connection", Toast.LENGTH_SHORT).show();
        }


    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    // add attendance to google sheet
    private void addItemToSheet(String Name, String Date) {


        final String name = Name;
        final String date = Date;


        // connects to app script
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://script.google.com/macros/s/AKfycbzUkq2_irwmC7p3UNYVWZ5uoNeWazEys3s7hGxtEzU7tuTc4F4/exec",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (!response.equals("")) {
                            Toast.makeText(Attendance.this, response, Toast.LENGTH_LONG).show();
                            Log.d("Js", "sd" + response);
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Json", error.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parmas = new HashMap<>();

                //here we pass params
                parmas.put("action", "addItem");
                parmas.put("name", name);
                parmas.put("date", date);

                return parmas;
            }
        };

        int socketTimeOut = 50000;// u can change this .. here it is 50 seconds

        RetryPolicy retryPolicy = new DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);

        RequestQueue queue = Volley.newRequestQueue(this);

        queue.add(stringRequest);


    }
}
