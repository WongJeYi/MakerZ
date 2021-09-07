package com.makerz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.makerz.model.MakerEvent;
import com.makerz.util.DownloadImageTask;
import com.makerz.util.FirebaseUtil;
import com.makerz.util.OnTaskCompleted;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.SSLSession;

public class AddNoteActivity extends AppCompatActivity {
    // UI variable

    private EditText etEventName;
    private EditText etEventDeadline;
    private EditText etEventDescription;
    private EditText etEventUrl;
    private EditText etEventTime;
    private EditText etEventEndTime;
    private RadioButton contest, trip, activity;
    private ImageView ChooseImage;
    private TextView tvChooseImage;
    private Button set;

    // Constants
    private static final String MAKERZ_PICTURE_FOLDER = "Picture";
    private static final String KEY_EMPTY = "";
    private final int PICK_IMAGE_CAMERA = 1, PICK_IMAGE_GALLERY = 2;

    // EventName is the name of event
    // EventDeadline is the date when the event closed
    // EventDescription is the description of the event, will be toasted when it is queried
    // EventUrl is the url or website of the event organizer or hosting company
    // EventImg is the image that represents the event
    // EventType is the type of event(Trip, contest, activity)
    static String EventName, EventDeadline, EventDescription, EventUrl, EventImg, EventType,EventTime,EventEndTime;

    // These are for capturing EventImg
    private String encodedImage = "";
    private Bitmap bitmap;
    private File destination = null;
    private String imgPath = null;

    //These are for capturing EventDeadline
    private int mDay, mMonth, mYear;

    // progressDialog is show before the request to firebase is done
    private ProgressDialog progressDialog;

    // These are used when opening the floating webView
    private int _yDelta;
    private WebView webView;
    private ViewGroup _root;
    private FloatingActionButton floatingActionButton;
    private EditText addressBar;
    private FloatingActionButton copyButton;
    private int down;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        // Initialize the view and functions of floating webview
        initfloat();
        // Setup UI
        setupUI();
        // etEventName on click will remove the error due to validation failed
        etEventName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etEventName.setError(null);
            }
        });
        // ChooseImage on click will remove the error due to validation failed
        ChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvChooseImage.setError(null);
                // Pops up the option menu for selecting image
                selectImage();
            }
        });
        // etEventDeadline on click will remove the error due to validation failed
        etEventDeadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etEventDeadline.setError(null);
                // Pops up the date picker dialog
                selectDate();
            }
        });
        // to remove the paste text option for etEventDeadline
        etEventDeadline.setLongClickable(false);
        // etEventUrl on click will remove the error due to validation failed
        etEventUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etEventUrl.setError(null);
            }
        });
        // etEventDescription on click will remove the error due to validation failed
        etEventDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etEventDescription.setError(null);
            }
        });
        etEventTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(AddNoteActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String Hour, Min;
                        if(selectedHour<=9){
                            Hour="0"+((Integer)selectedHour).toString();
                        }else{
                            Hour=((Integer)selectedHour).toString();
                        }
                        if(selectedMinute<=9){
                            Min="0"+((Integer)selectedMinute).toString();
                        }else{
                            Min=((Integer)selectedMinute).toString();
                        }
                        etEventTime.setText(Hour + ":" + Min);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });
        etEventEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(AddNoteActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String Hour, Min;
                        if(selectedHour<=9){
                            Hour="0"+((Integer)selectedHour).toString();
                        }else{
                            Hour=((Integer)selectedHour).toString();
                        }
                        if(selectedMinute<=9){
                            Min="0"+((Integer)selectedMinute).toString();
                        }else{
                            Min=((Integer)selectedMinute).toString();
                        }
                        etEventEndTime.setText(Hour + ":" + Min);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });
        // set on click will start to validate the event and add to firebase
        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get text of each inputs
                EventName = etEventName.getText().toString().trim();
                EventDeadline = etEventDeadline.getText().toString().trim();
                EventUrl = etEventUrl.getText().toString().trim();
                EventDescription = etEventDescription.getText().toString().trim();
                EventImg = encodedImage;
                EventTime=etEventTime.getText().toString().trim();
                EventEndTime=etEventEndTime.getText().toString().trim();
                // validate each inputs
                if (validateInputs()) {
                    // progressDialog is showed until progress finish
                    progressDialog = new ProgressDialog(AddNoteActivity.this);
                    progressDialog.setMessage("Loading...");
                    progressDialog.show();
                    // upload to firebase
                    uploadEvent();
                } else {
                    Toast.makeText(AddNoteActivity.this, "Not yet complete", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupUI() {
        contest = findViewById(R.id.radio_contest);
        activity = findViewById(R.id.radio_activity);
        trip = findViewById(R.id.radio_trip);
        // trip is only for admin user to create, normal member cannot access
        trip.setVisibility(View.GONE);
        etEventName = ((EditText) findViewById(R.id.event_name));
        etEventDeadline = ((EditText) findViewById(R.id.event_deadline));
        ChooseImage = findViewById(R.id.event_thumbnail);
        tvChooseImage = findViewById(R.id.event_img_url);
        etEventUrl = ((EditText) findViewById(R.id.event_url));
        etEventDescription = ((EditText) findViewById(R.id.event_description));
        etEventTime=findViewById(R.id.event_time);
        etEventEndTime=findViewById(R.id.event_end_time);
        set = findViewById(R.id.button_set);
    }

    private void initfloat() {
        // copyButton is a button to copy url
        copyButton = new FloatingActionButton(this);
        copyButton.setImageResource(R.drawable.copy_button);
        // to give the gray tint when text is selected
        copyButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));

        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // copy to clipboard
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                String text = addressBar.getText().toString();
                android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getApplicationContext(), "Copied Text", Toast.LENGTH_SHORT).show();
            }
        });
        // addressBar is for entering url and copy url
        addressBar = new EditText(this);
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(8);
        shape.setColor(Color.WHITE);
        // floatingActionButton is the floating head that opens and close the webview
        floatingActionButton = new FloatingActionButton(this);
        floatingActionButton.setImageResource(R.drawable.chrome_button);
        floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        // webView is loading google upon initiation
        webView = new WebView(this);
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.setLongClickable(true);
        // to enable javascripts
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setBuiltInZoomControls(true);
        // zoom if you want
        webView.getSettings().setSupportZoom(true);
        // to support url redirections
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                addressBar.setText(webView.getUrl());
                super.onPageFinished(view, url);
            }
        });
        // extra settings
        webView.getSettings().setLoadWithOverviewMode(false);
        webView.getSettings().setUseWideViewPort(true);
        webView.setScrollContainer(true);
        // setting for lollipop and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        // load google
        webView.loadUrl("https://www.google.com");

        // _root is the rootview of the activity
        _root = (ViewGroup) findViewById(R.id.boss_layout);
        _root.setFocusable(true);

        //layout definition
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        final int dpWidth = displayMetrics.widthPixels;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = 0;
        layoutParams.topMargin = 50;
        layoutParams.bottomMargin = -250;
        layoutParams.rightMargin = -250;
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.addRule(RelativeLayout.ALIGN_LEFT);
        floatingActionButton.setLayoutParams(layoutParams);
        RelativeLayout.LayoutParams layoutParamsC = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParamsC.leftMargin = -250;
        layoutParamsC.topMargin = 50;
        layoutParamsC.bottomMargin = -250;
        layoutParamsC.rightMargin = 50;
        layoutParamsC.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParamsC.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        copyButton.setLayoutParams(layoutParamsC);
        addressBar.setPadding(10, 0, 0, 0);
        addressBar.setBackground(shape);
        addressBar.setBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
        RelativeLayout.LayoutParams layoutParamsA = new RelativeLayout.LayoutParams(dpToPx(250), dpToPx(40));
        layoutParamsA.leftMargin = 200;
        layoutParamsA.topMargin = 100;
        layoutParamsA.bottomMargin = -250;
        layoutParamsA.rightMargin = -250;
        layoutParamsA.addRule(RelativeLayout.ALIGN_LEFT);
        layoutParamsA.addRule(RelativeLayout.BELOW, floatingActionButton.getId());
        addressBar.setLayoutParams(layoutParamsA);
        webView.setBackground(shape);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        RelativeLayout.LayoutParams layoutParamsW = new RelativeLayout.LayoutParams(dpWidth, dpToPx(500));
        layoutParamsW.leftMargin = 0;
        layoutParamsW.topMargin = 250;
        layoutParamsW.bottomMargin = -250;
        layoutParamsW.rightMargin = -250;
        layoutParamsW.addRule(RelativeLayout.ALIGN_LEFT);
        layoutParamsW.addRule(RelativeLayout.BELOW, addressBar.getId());
        webView.setLayoutParams(layoutParamsW);

        // hide all except floatingActionButton
        copyButton.hide();
        webView.setVisibility(View.GONE);
        addressBar.setVisibility(View.GONE);
        // disable longclick for webView
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return false;
            }
        });


        // enable search button in keyboard
        addressBar.setInputType(InputType.TYPE_CLASS_TEXT);
        addressBar.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        addressBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String text = addressBar.getText().toString();
                    // First step for showing trip radio button
                    // Enter "Admin" in the addressBar
                    if (text.equals("Admin")) {
                        Log.d("went", "in");
                        // Prompt for Password, "MakerZ123"
                        AlertDialog.Builder builder = new AlertDialog.Builder(AddNoteActivity.this, R.style.AlertDialog);
                        builder.setTitle("Enter Password :");

                        final EditText password = new EditText(AddNoteActivity.this);
                        password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        builder.setView(password);
                        builder.setPositiveButton("Yes", null);
                        final AlertDialog dialog = builder.create();
                        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialogInterface) {
                                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                                button.setOnClickListener(new View.OnClickListener() {
                                    int times = 5;

                                    @Override
                                    public void onClick(View view) {
                                        // TODO Do something
                                        final String Password = password.getText().toString();

                                        if (TextUtils.isEmpty(Password)) {
                                            Toast.makeText(AddNoteActivity.this, "Please enter the password", Toast.LENGTH_SHORT).show();
                                        } else {
                                            if (Password.equals("MakerZ123")) {
                                                // Show trip radio button
                                                trip.setVisibility(View.VISIBLE);
                                                dialog.dismiss();
                                            } else {
                                                times--;
                                                Toast.makeText(AddNoteActivity.this, "Remaining number of try: " + times, Toast.LENGTH_SHORT).show();
                                                if (times == 0) {
                                                    dialog.dismiss();
                                                }
                                            }
                                        }

                                    }
                                });
                            }
                        });
                        dialog.show();

                    } else {
                        // load url entered to addressBar
                        webView.loadUrl(text);
                        addressBar.clearFocus();
                        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        in.hideSoftInputFromWindow(addressBar.getWindowToken(), 0);
                    }

                    return true;
                }
                return false;
            }
        });


        // add to root
        _root.addView(webView);
        _root.addView(floatingActionButton);
        _root.addView(addressBar);
        _root.addView(copyButton);

        // webView will register a menu that prompts when long click on image
        registerForContextMenu(webView);

        // Adjust layout on Dragging
        floatingActionButton.setOnTouchListener(new View.OnTouchListener() {
            boolean moved = false;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                final int X = (int) motionEvent.getRawX();
                final int Y = (int) motionEvent.getRawY();
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:

                        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                        down = Y;
                        _yDelta = Y - lParams.topMargin;
                        // set webview visibility
                        break;
                    case MotionEvent.ACTION_UP:
                        lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                        if (Y == down) {
                            moved = false;
                        }
                        if (!moved)
                            if (webView.getVisibility() == View.GONE) {
                                webView.setVisibility(View.VISIBLE);
                                addressBar.setVisibility(View.VISIBLE);
                                copyButton.show();
                            } else {
                                webView.setVisibility(View.GONE);
                                addressBar.setVisibility(View.GONE);
                                copyButton.hide();
                                addressBar.clearFocus();
                                // clear keyboard
                                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                in.hideSoftInputFromWindow(addressBar.getWindowToken(), 0);
                            }
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        moved = true;
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                        layoutParams.topMargin = Y - _yDelta;
                        layoutParams.rightMargin = -250;
                        layoutParams.bottomMargin = -250;
                        view.setLayoutParams(layoutParams);
                        RelativeLayout.LayoutParams layoutParamsW = (RelativeLayout.LayoutParams) webView.getLayoutParams();
                        layoutParamsW.topMargin = Y - _yDelta + 200;
                        layoutParamsW.rightMargin = -250;
                        layoutParamsW.bottomMargin = -250;
                        webView.setLayoutParams(layoutParamsW);
                        RelativeLayout.LayoutParams layoutParamsA = (RelativeLayout.LayoutParams) addressBar.getLayoutParams();
                        layoutParamsA.topMargin = Y - _yDelta + 50;
                        layoutParamsA.rightMargin = -250;
                        layoutParamsA.bottomMargin = -250;
                        addressBar.setLayoutParams(layoutParamsA);
                        RelativeLayout.LayoutParams layoutParamsC = (RelativeLayout.LayoutParams) copyButton.getLayoutParams();
                        layoutParamsC.topMargin = Y - _yDelta;
                        layoutParamsC.rightMargin = 50;
                        layoutParamsC.bottomMargin = -250;
                        copyButton.setLayoutParams(layoutParamsC);
                        break;
                }
                _root.invalidate();
                return true;
            }
        });
        SharedPreferences spref = PreferenceManager.getDefaultSharedPreferences(this);

        // Creating an Editor object to edit(write to the file)
        SharedPreferences.Editor myEdit = spref.edit();

        if (spref.getBoolean("FirstTimeAddNote", true)) {
            myEdit.putBoolean("FirstTimeAddNote", false);
            myEdit.commit();
            TapTargetView.showFor(AddNoteActivity.this, TapTarget.forView(floatingActionButton, "This is a web chrome", "You can browse for new events by clicking here")
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
                    webView.setVisibility(View.VISIBLE);
                    addressBar.setVisibility(View.VISIBLE);
                    copyButton.show();
                    TapTargetView.showFor(AddNoteActivity.this, TapTarget.forView(copyButton, "This is a copy button", "You can copy address by clicking here")
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
                            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            String text = addressBar.getText().toString();
                            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(getApplicationContext(), "Copied Text", Toast.LENGTH_SHORT).show();
                            Rect rect= new Rect(dpWidth/2-1,750,dpWidth/2+1,750);
                            TapTargetView.showFor(AddNoteActivity.this, TapTarget.forBounds(rect, "Long click to use image", "You can use image by long clicking on the image")
                                    .outerCircleColor(R.color.colorPrimaryDark)
                                    .outerCircleAlpha(0.06f)
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
                                    TapTargetView.showFor(AddNoteActivity.this, TapTarget.forView(floatingActionButton, "Close the web chrome", "You can close the web chrome by reclicking here")
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
                                            webView.setVisibility(View.GONE);
                                            addressBar.setVisibility(View.GONE);
                                            copyButton.hide();
                                            addressBar.clearFocus();
                                            // clear keyboard
                                            InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                            in.hideSoftInputFromWindow(addressBar.getWindowToken(), 0);
                                            TapTargetView.showFor(AddNoteActivity.this, TapTarget.forView(set, "This is the set event button", "You can set new events by clicking here")
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
                                                    startActivity(new Intent(AddNoteActivity.this,Attendance.class));
                                                    finish();
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            });
        }
    }

    // To save image from web view
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        // Get the web view hit test result
        final WebView.HitTestResult result = webView.getHitTestResult();


        // If user long press on an image
        if (result.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {

            // Set the title for context menu
            menu.setHeaderTitle("Event");

            // Add an item to the menu
            menu.add(0, 1, 0, "Use Image")
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            // Get the image url
                            String imgUrl = result.getExtra();
                            new DownloadImageTask((ImageView) ChooseImage, new OnTaskCompleted() {
                                @Override
                                public void onTaskCompleted(Bitmap bitmap, String encodedimage) {
                                    if (bitmap != null) {
                                        tvChooseImage.setVisibility(View.GONE);
                                        encodedImage=encodedimage;
                                        Toast.makeText(getApplicationContext(), "image saved.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Invalid image url, try to get from the origin website.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                                    .execute(imgUrl);
                            return false;
                        }
                    });
        }
    }

    // Override back button for webview and activity
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (webView.canGoBack()) {
                        // go  back to previous page
                        webView.goBack();
                    } else if (webView.getVisibility() == View.VISIBLE) {
                        // close webview
                        webView.setVisibility(View.GONE);
                        addressBar.setVisibility(View.GONE);
                        copyButton.hide();
                        addressBar.clearFocus();
                        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        in.hideSoftInputFromWindow(addressBar.getWindowToken(), 0);
                    } else {
                        // finish on back pressed
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    private void uploadEvent() {
        // get Database
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();


        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            // get database reference
            final DatabaseReference databaseRefer = firebaseDatabase.getReference().child("Events");
            // database listener
            ValueEventListener postListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // check event exists
                    if (dataSnapshot.hasChild(EventName)) {
                        // retrun failed
                        etEventName.setError("Event exists");
                        progressDialog.dismiss();
                    } else {

                        MakerEvent makerEvent = new MakerEvent(EventName, EventUrl, EventImg, EventDescription, EventDeadline, EventType, FirebaseAuth.getInstance().getCurrentUser().getEmail(),EventTime,EventEndTime,false);
                        databaseRefer.child(EventName).setValue(makerEvent);
                        progressDialog.dismiss();
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            // add listener
            databaseRefer.addValueEventListener(postListener);

        } else {
            Toast.makeText(this, "No connection", Toast.LENGTH_SHORT);
        }


    }

    private boolean validateInputs() {
        // validate filled?
        Log.d("VAr",EventName+" "+EventImg+" "+EventDeadline+" "+EventDescription+" "+EventUrl);
        if (KEY_EMPTY.equals(EventName)) {
            etEventName.setError("Event Name cannot be empty");
            etEventName.requestFocus();
            return false;

        }
        if (KEY_EMPTY.equals(EventImg)) {
            tvChooseImage.setError("Event Img cannot be empty");
            Toast.makeText(this, "Event Img cannot be empty", Toast.LENGTH_SHORT);
            ChooseImage.requestFocus();
            return false;
        }
        if (KEY_EMPTY.equals(EventDeadline)) {
            etEventDeadline.setError("Event Deadline cannot be empty");
            etEventDeadline.requestFocus();
            return false;
        }
        if (KEY_EMPTY.equals(EventDescription)) {
            etEventDescription.setError("Event Description cannot be empty");
            etEventDescription.requestFocus();
            return false;
        }

        if (KEY_EMPTY.equals(EventUrl)) {
            etEventUrl.setError("Event Url cannot be empty");
            etEventUrl.requestFocus();
            return false;
        }
        if (KEY_EMPTY.equals(EventTime)) {
            etEventTime.setError("Event Time cannot be empty");
            etEventTime.requestFocus();
            return false;
        }
        if (KEY_EMPTY.equals(EventEndTime)) {
            etEventEndTime.setError("Event end time cannot be empty");
            etEventEndTime.requestFocus();
            return false;
        }
        SimpleDateFormat dateFormat=new SimpleDateFormat("HH:mm");
        Date start, end;
        try {
            start=dateFormat.parse(EventTime);

        end=dateFormat.parse(EventEndTime);
        if (start.compareTo(end)>=0&& !EventEndTime.matches("00:00")) {
            etEventEndTime.getText().clear();
            etEventEndTime.setError("Event end time cannot be earlier than start");
            etEventEndTime.requestFocus();
            return false;
        }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (activity.isChecked()) {
            EventType = "Activity";
        } else if (contest.isChecked()) {
            EventType = "Contest";
        } else if (trip.isChecked()) {
            EventType = "Trip";
        } else {
            activity.setError("Choose one");
            return false;
        }
        // check webUrl exists
        boolean check = URLUtil.isValidUrl(EventUrl);

        if (check == false) {
            etEventUrl.setError("Website not valid");
            etEventUrl.requestFocus();
            return false;
        } else {
            return true;
        }

    }

    // Select image from camera and gallery
    private void selectImage() {
        try {
            PackageManager pm = getPackageManager();
            int hasPerm = pm.checkPermission(Manifest.permission.CAMERA, getPackageName());
            if (hasPerm == PackageManager.PERMISSION_GRANTED) {

                final CharSequence[] options = {"Take Photo", "Choose From Gallery", "Paste Image URL", "Cancel"};

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select Option");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("Take Photo")) {
                            dialog.dismiss();
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, PICK_IMAGE_CAMERA);
                        } else if (options[item].equals("Choose From Gallery")) {
                            dialog.dismiss();
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto, PICK_IMAGE_GALLERY);
                        } else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        } else if (options[item].equals("Paste Image URL")) {
                            dialog.dismiss();
                            // alert dialog for  url
                            AlertDialog.Builder builder = new AlertDialog.Builder(AddNoteActivity.this, R.style.AlertDialog);
                            builder.setTitle("Enter URL :");

                            final EditText urlField = new EditText(AddNoteActivity.this);
                            urlField.setHint("e.g http://www.google.com");
                            builder.setView(urlField);


                            builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    final String url = urlField.getText().toString();

                                    if (TextUtils.isEmpty(url)) {
                                        Toast.makeText(AddNoteActivity.this, "Please enter the image url", Toast.LENGTH_SHORT).show();
                                    } else {
                                        new DownloadImageTask((ImageView) ChooseImage, new OnTaskCompleted() {
                                            @Override
                                            public void onTaskCompleted(Bitmap bitmap, String encodedimage) {
                                                if (bitmap != null) {
                                                    tvChooseImage.setVisibility(View.GONE);
                                                    encodedImage=encodedimage;
                                                    Toast.makeText(getApplicationContext(), "image saved.", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(getApplicationContext(), "Invalid image url, try to get from the origin website.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        })
                                                .execute(url);
                                    }
                                }

                            });

                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });
                            builder.show();

                        }
                    }
                });
                builder.show();
            } else
                Toast.makeText(this, "Camera Permission error", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Camera Permission error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_CAMERA) {
            try {
                // get bitmap
                Uri selectedImage = data.getData();
                bitmap = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytes);

                Log.d("Activity", "Pick from Camera::>>> ");
                // store as temp file
                destination = new File(getInternalStorage("Temp"));
                FileOutputStream fo;
                byte[] imagebyte = bytes.toByteArray();
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(imagebyte);
                    fo.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // set image
                imgPath = destination.getAbsolutePath();
                tvChooseImage.setVisibility(View.GONE);
                ChooseImage.setImageURI((Uri.parse(imgPath)));

                // image into base64 encoded image to store in database
                encodedImage = Base64.encodeToString(imagebyte, Base64.DEFAULT);
                Log.d("url", imgPath);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == PICK_IMAGE_GALLERY) {
            if(data!=null) {
                Uri selectedImage = data.getData();
                try {
                    // get bitmap
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
                    Log.d("Activity", "Pick from Gallery::>>> ");
                    //set image
                    imgPath = getRealPathFromURI(selectedImage);
                    destination = new File(imgPath.toString());
                    tvChooseImage.setVisibility(View.GONE);
                    ChooseImage.setImageURI((Uri.parse(imgPath)));
                    // image into base64 encoded image to store in database
                    encodedImage = Base64.encodeToString(bytes.toByteArray(), Base64.DEFAULT);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public void selectDate() {
        Calendar mcurrentDate = Calendar.getInstance();
        mYear = mcurrentDate.get(Calendar.YEAR);
        mMonth = mcurrentDate.get(Calendar.MONTH);
        mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

        final DatePickerDialog mDatePicker = new DatePickerDialog(AddNoteActivity.this, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                // generate selected date as myCalendaar
                Calendar myCalendar = Calendar.getInstance();
                myCalendar.set(Calendar.YEAR, selectedyear);
                myCalendar.set(Calendar.MONTH, selectedmonth);
                myCalendar.set(Calendar.DAY_OF_MONTH, selectedday);
                String myFormat = "yyyy-MM-dd"; //Change as you need
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
                String date = sdf.format(myCalendar.getTime());
                String today = sdf.format(new Date());
                mDay = selectedday;
                mMonth = selectedmonth;
                mYear = selectedyear;

                etEventDeadline.setText(date);

            }
        }, mYear, mMonth, mDay);
        //mDatePicker.setTitle("Select date");
        Calendar today = Calendar.getInstance();
        mDatePicker.getDatePicker().setMinDate(today.getTimeInMillis());
        mDatePicker.show();

    }

    private String getInternalStorage(String filename) {
        ContextWrapper cw = new ContextWrapper(this);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir(MAKERZ_PICTURE_FOLDER, Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, filename + ".jpg");
        return mypath.toString();
    }



    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
