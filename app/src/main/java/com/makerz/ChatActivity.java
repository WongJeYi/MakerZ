package com.makerz;

import android.Manifest;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ClipDescription;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaRecorder;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.gsconrad.richcontentedittext.RichContentEditText;
import com.makerz.Notifications.APIService;
import com.makerz.Notifications.Client;
import com.makerz.Notifications.Notification;
import com.makerz.Notifications.Response;
import com.makerz.Notifications.Sender;
import com.makerz.adapter.MainChatAdapter;
import com.makerz.model.Message;
import com.makerz.model.user;
import com.makerz.util.ExampleDialog;
import com.makerz.util.FirebaseUtil;
import com.makerz.util.LoadingDialog;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.makerz.Attendance.hasPermissions;


// This activity is used for public chat for all MakerZ members.

// After pressing GIF, the keyboard does not hide on its own

public class ChatActivity extends AppCompatActivity implements ExampleDialog.ExampleDialogListener{

    private final List<Message> messageList = new ArrayList<>();
    ArrayList<String> groupChatNames = new ArrayList<>();
    private String messageReceiverID, messageReceiverName, messageReceiverImage;
    private String messagePushID, messageInput;
    private String currentGroupName, userId, currentUserName,  saveCurrentDate, saveCurrentTime, currentFullName;

    String userFullnameForGroupChat, userCodenameForGroupChat;
    private String fullname, codename, groupName;

    int verify = 0;
    int check = 0;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference myRef, GroupNameRef;
    private StorageReference imageReference;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser currentUser;
    private user mUser;
    private DatabaseReference databaseReference;

    private LinearLayoutManager linearLayoutManager;
    private MainChatAdapter chatAdapter;
    RecyclerView rvMessage;
    Intent intent;

    private DrawerLayout myDrawer;
    private NavigationView NavigationView;
    private ActionBarDrawerToggle myToggle;
    private Toolbar toolbar;
    private ImageView profilePic;
    private View header;

    private RichContentEditText userMessageInput;
    private ImageButton sendButton;
    private ImageButton clipButton;
    private ImageButton microphoneButton;
    private ImageButton cameraButton;

    StorageReference filePath, audiofilePath, giffilePath;
    StorageReference storageReference;
    Uri fileUri;

    final LoadingDialog loadingDialog = new LoadingDialog(ChatActivity.this);

    private String checker="", myUri="";
    private StorageTask uploadTask;
    private ProgressDialog loadingBar;

    String currentPhotoPath;
    Uri photoURI;
    public String docUri;

    private MediaRecorder recorder = null;
    private String fileName = null;
    private static final String LOG_TAG = "Record_log";
    boolean isRecording = false;
    boolean longClick = false;
    float x1;

    File richContentFile;
    APIService apiService;
    //String type;
    private String[] retImage = {"default_image"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Declaration of elements.
        setupUIViews();
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                // Request for camera permission.
                Manifest.permission.CAMERA,
                // Request for audio permission.
                Manifest.permission.RECORD_AUDIO,
                // Request for gallery permission.
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };

        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);
        //Subscribe to notification

        subscribeToNotification(getString(R.string.main_chat)+"MakerZ");
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        // Display drawer on the toolbar hamburger icon click.
        setSupportActionBar(toolbar);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
       /* NavigationView.bringToFront();*/


        myToggle = new ActionBarDrawerToggle(this, myDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        myDrawer.addDrawerListener(myToggle);
        myToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_hamburger);

        loadingBar = new ProgressDialog(ChatActivity.this);

        // Values passed from MenuActivity / ContentstuffActivity / ViewGroupActivity
        if (getIntent() != null && getIntent().getExtras() != null)
        {Bundle bundle = getIntent().getExtras();
            for(String key:bundle.keySet()){
            Log.e("Key",key+bundle.get(key));
        }
            currentGroupName = "MakerZ";
            messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
            messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
            messageReceiverImage = getIntent().getExtras().get("visit_image").toString();

        }

        // Get current user's info.
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference("Images");

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        userId = firebaseAuth.getCurrentUser().getUid();


        firebaseDatabase = FirebaseDatabase.getInstance();
        myRef =FirebaseDatabase.getInstance().getReference();
        imageReference = storageReference.child("Profile Picture").child(userId + "/" + "profilepic.jpg");

        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Messages").child("Main Group").child(currentGroupName);

        GetUserInfo();

        // To allow GIF Insertion for EditText.
        userMessageInput.setOnRichContentListener(new RichContentEditText.OnRichContentListener() {
            // Called when a keyboard sends rich content
            @Override
            public void onRichContent(Uri contentUri, ClipDescription description) {
                if (description.getMimeTypeCount() > 0) {
                    final String fileExtension = MimeTypeMap.getSingleton()
                            .getExtensionFromMimeType(description.getMimeType(0));
                    final String filename = "filenameGoesHere." + fileExtension;
                    richContentFile = new File(getFilesDir(), filename);

                    if (!writeToFileFromContentUri(richContentFile, contentUri)) {

                        Toast.makeText(ChatActivity.this,
                                "Failed to send the GIF Image.", Toast.LENGTH_LONG).show();
                    } else {
                        // Hide keyboard after successfully sending message.
                        View view = ChatActivity.this.getCurrentFocus();

                        if (view !=null) {
                            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }

                        SaveMessageInfoToDatabaseForGif();
                    }
                }
            }
        });

        // Buttons.
        // sendButton is used to send text/emoji messages.
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               // notify = true;

                // Hide keyboard after successfully sending message.
                View view = ChatActivity.this.getCurrentFocus();

                if (view !=null) {
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                // Save message to database.
                SaveMessageInfoToDatabaseForText();
                userMessageInput.setText("");
            }
        });

        // clipButton is used to send PDF or Microsoft Word files.
        clipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CharSequence choices[] = new CharSequence[]
                        {
                                "PDF Files",      // pdf
                                "Ms Word Files"   // docx
                        };

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this,R.style.AlertDialog);
                builder.setTitle("Select your file.");

                builder.setItems(choices, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (i == 0) {

                            checker = "pdf";
                            Intent  contentIntent = new Intent();
                            contentIntent.setAction(Intent.ACTION_GET_CONTENT);
                            contentIntent.setType("application/pdf");
                            startActivityForResult(contentIntent.createChooser(contentIntent,"Select PDF File"), 438);

                        } else if (i == 1) {

                            checker = "docx";
                            Intent  contentIntent = new Intent();
                            contentIntent.setAction(Intent.ACTION_GET_CONTENT);
                            contentIntent.setType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
                            startActivityForResult(contentIntent.createChooser(contentIntent,"Select Ms Word File"), 438);

                        }
                    }
                });
                builder.show();

            }
        });

        // cameraButton is used to send pictures.
        // However, videos sending function is not added,
        // due to 1GB limited realtime database capacity for firebase per day.
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CharSequence options[] = new CharSequence[]
                        {"Gallery", "Camera"};

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this,R.style.AlertDialog);
                builder.setTitle("Select your option.");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (i == 0)
                        {
                            checker = "gallery";

                            // Open gallery
                            // 438 is just a request code, can put whatever value
                            Intent  contentIntent = new Intent();
                            contentIntent.setAction(Intent.ACTION_GET_CONTENT);
                            contentIntent.setType("image/*");
                            startActivityForResult(contentIntent.createChooser(contentIntent,"Select Image"), 438);

                        }
                        if (i == 1)
                        {
                            checker = "camera";

                            dispatchTakePictureIntent();

                        }
                    }
                });
                builder.show();
            }
        });

        // microphoneButton is used to record audios.
        microphoneButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, final MotionEvent event) {

                // As click action will invoke both ACTION_DOWN and ACTION_UP,
                // ACTION_MOVE is used to determine if it is a click action or long press.
                // If it is a click action, the recording will not start.
                // Else if it is a long press, the recording will start.


                if(event.getAction() == MotionEvent.ACTION_DOWN){

                    longClick = false;
                    x1 = event.getX();
                    startRecording();
                    Toast.makeText(ChatActivity.this,"Recording started...",Toast.LENGTH_SHORT).show();

                } else  if (event.getAction() == MotionEvent.ACTION_MOVE){

                    if(event.getEventTime() - event.getDownTime() > 500){
                        longClick = true;
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP){

                    if(longClick){
                        stopRecording();
                        Toast.makeText(ChatActivity.this,"Recording stopped...",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(ChatActivity.this,"Please long-press on the button to record audio.",Toast.LENGTH_SHORT).show();

                    }

                }
                return false;
            }
        });


        GroupNameRef.child("All Messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {

                Message message = dataSnapshot.getValue(Message.class);
                messageList.add(message);
                chatAdapter.notifyDataSetChanged();
                rvMessage.smoothScrollToPosition(rvMessage.getAdapter().getItemCount());

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
            {
                Message messageremoved = dataSnapshot.getValue(Message.class);

                for(int i = 0; i < messageList.size(); i++)
                {
                    assert messageremoved != null;

                    if(messageList.get(i).getMessageID().equals(messageremoved.getMessageID()))
                    {
                        messageList.remove(i);
                        chatAdapter.notifyItemRemoved(i);
                        chatAdapter.notifyDataSetChanged();
                        break;
                    }

                }

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // When drawer is opened, there will be 4 selection of items to choose from.
        NavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);

                switch (menuItem.getItemId()) {
                    case R.id.add_group:
                        AddNewGroup();
                        break;

                    case R.id.view_group:
                        startActivity(new Intent(ChatActivity.this, ViewGroupActivity.class));
                        finish();
                        break;

                    case R.id.home:
                        startActivity(new Intent(ChatActivity.this,MenuActivity.class));
                        finish();
                        break;

                    case R.id.button_backz3:
                        myDrawer.closeDrawers();
                        break;

                    default:
                        myDrawer.closeDrawers();
                        return true;
                }
                myDrawer.closeDrawers();
                return true;
            }
        });
        SharedPreferences spref = PreferenceManager.getDefaultSharedPreferences(this);

        // Creating an Editor object to edit(write to the file)
        SharedPreferences.Editor myEdit = spref.edit();

        if (spref.getBoolean("FirstTimeChat", true)) {
            myEdit.putBoolean("FirstTimeChat", false);
            myEdit.commit();
            TapTargetView.showFor(ChatActivity.this, TapTarget.forView(clipButton, "This is the document attachment button", "You can attach documents by clicking here")
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
                    .targetRadius(40)
                    .transparentTarget(true), new TapTargetView.Listener() {
                @Override
                public void onTargetClick(TapTargetView view) {
                    super.onTargetClick(view);
                    view.dismiss(true);
                    TapTargetView.showFor(ChatActivity.this, TapTarget.forView(microphoneButton, "This is the voice recording button", "Hold to record voice")
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
                            .targetRadius(40)
                            .transparentTarget(true), new TapTargetView.Listener() {
                        @Override
                        public void onTargetClick(TapTargetView view) {
                            super.onTargetClick(view);
                            view.dismiss(true);
                            TapTargetView.showFor(ChatActivity.this, TapTarget.forView(cameraButton, "This is the Camera button", "You can attach image by clicking here")
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
                                    .targetRadius(40)
                                    .transparentTarget(true), new TapTargetView.Listener() {
                                @Override
                                public void onTargetClick(TapTargetView view) {
                                    super.onTargetClick(view);
                                    view.dismiss(true);
                                    TapTargetView.showFor(ChatActivity.this, TapTarget.forView(sendButton, "This is the send button", "You can send message by clicking here")
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
                                            .targetRadius(40)
                                            .transparentTarget(true), new TapTargetView.Listener() {
                                        @Override
                                        public void onTargetClick(TapTargetView view) {
                                            super.onTargetClick(view);
                                            view.dismiss(true);
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

    private void startRecording() {
        if (recorder != null)
        {
            recorder.reset();
            recorder.release();
        }
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("recordDir", Context.MODE_PRIVATE);
        fileName = (new File(directory, "recording" + ".3gp")).toString();

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        recorder.setOutputFile(fileName);
            try {
                recorder.prepare();
                recorder.start();

            } catch (IllegalStateException  e) {
                Log.e(LOG_TAG, "prepare() failed");
                userMessageInput.setText(userMessageInput.getText()+"Error: "+e.getMessage()+"IL");
            } catch (IOException e) {
                e.printStackTrace();
                userMessageInput.setText(userMessageInput.getText()+"Error: "+e.getMessage()+"IO");
            }

            isRecording = true;


    }

    private void stopRecording() {
        if (recorder != null) {
            try {
                recorder.stop();
                recorder.reset();
                recorder.release();
                recorder = null;
                SaveMessageInfoToDatabaseForAudio();

            } catch (IllegalStateException e) {
                e.printStackTrace();
                userMessageInput.setText(userMessageInput.getText()+"Error: "+e.getMessage()+"ILE");
            }
        }
    }

    private void dispatchTakePictureIntent() {
        // Open camera.
        // 438 is just a request code.

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent.
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            // Create the File where the photo should go.
            File photoFile = null;
            try {
                photoFile = createImageFile();

            } catch (IOException ex) {

                // Error occurred while creating the File.
                Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();

            }

            // Continue only if the File was successfully created.
            if (photoFile != null) {

                photoURI = FileProvider.getUriForFile(this,
                        "com.makerz.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 438);
            }
        }

    }

    private File createImageFile() throws IOException {

        // Create an image file name.
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents.
        currentPhotoPath = image.getAbsolutePath();
        return image;

    }

    private void SaveMessageInfoToDatabaseForText() {
        final String messageInput = userMessageInput.getText().toString();

        if (!messageInput.equals(""))
        {

            GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Messages").child("Main Group").child(currentGroupName);
            DatabaseReference userMessageKeyRef = GroupNameRef.push();

            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageInput);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", userId);
            messageTextBody.put("name", currentUserName);
            messageTextBody.put("to", currentGroupName);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);


            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put("All Messages/" + messagePushID, messageTextBody);

            GroupNameRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if (task.isSuccessful())
                    {
                        // Getting through here
                        sendNotification(currentFullName, currentUserName,messageInput);


                        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                        database.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if (dataSnapshot.hasChild("Images")) {
                                    retImage[0] = dataSnapshot.child("Images").getValue().toString();
                                }

                               /* String userCodename = dataSnapshot.child("codename").getValue(String.class);
                                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);*/



                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                    else
                    {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
        else
        {
            Toast.makeText(ChatActivity.this,"You can't send an empty message.",  Toast.LENGTH_SHORT).show();
        }

    }

    private void SaveMessageInfoToDatabaseForAudio() {
        loadingBar.setTitle("Sending File");
        loadingBar.setMessage("Please wait, we are sending that file...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        storageReference = FirebaseStorage.getInstance().getReference("Files");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Messages").child("Main Group").child(currentGroupName);
        DatabaseReference userMessageKeyRef = GroupNameRef.push();
        messagePushID = userMessageKeyRef.getKey();

        Uri audioUri = Uri.fromFile(new File(fileName));

        audiofilePath = storageReference.child("Main Group").child(currentGroupName).child(messagePushID+ "." + "3gp");
        audiofilePath.putFile(audioUri).continueWithTask(new Continuation(){
            @Override
            public Object then(@NonNull Task task) throws Exception {

                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return audiofilePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {

                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    myUri = downloadUri.toString();

                    Map messageTextBody = new HashMap();
                    messageTextBody.put("message", myUri);
                    messageTextBody.put("type", "audio");
                    messageTextBody.put("from", userId);
                    messageTextBody.put("name", currentUserName);
                    messageTextBody.put("to", currentGroupName);
                    messageTextBody.put("messageID", messagePushID);
                    messageTextBody.put("time", saveCurrentTime);
                    messageTextBody.put("date", saveCurrentDate);

                    Map messageBodyDetails = new HashMap();
                    messageBodyDetails.put("All Messages/" + messagePushID, messageTextBody);

                    GroupNameRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {

                            if (task.isSuccessful())
                            {
                                loadingBar.dismiss();
                            } else {

                                loadingBar.dismiss();
                                Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                            }
                            userMessageInput.setText("");
                        }
                    });


                } else {
                    Toast.makeText(ChatActivity.this, "Failed to send the audio.", Toast.LENGTH_SHORT).show();
                }

            }

        });
    }

    private void SaveMessageInfoToDatabaseForGif() {

        storageReference = FirebaseStorage.getInstance().getReference("GIF Images");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Messages").child("Main Group").child(currentGroupName);
        DatabaseReference userMessageKeyRef = GroupNameRef.push();
        messagePushID = userMessageKeyRef.getKey();

        Uri gifUri = Uri.fromFile(richContentFile);

        giffilePath = storageReference.child("Main Group").child(currentGroupName).child(messagePushID+ "." + "gif");
        giffilePath.putFile(gifUri).continueWithTask(new Continuation(){
            @Override
            public Object then(@NonNull Task task) throws Exception {

                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return giffilePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {

                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    myUri = downloadUri.toString();

                    Map messageTextBody = new HashMap();
                    messageTextBody.put("message", myUri);
                    messageTextBody.put("type", "gif");
                    messageTextBody.put("from", userId);
                    messageTextBody.put("name", currentUserName);
                    messageTextBody.put("to", currentGroupName);
                    messageTextBody.put("messageID", messagePushID);
                    messageTextBody.put("time", saveCurrentTime);
                    messageTextBody.put("date", saveCurrentDate);

                    Map messageBodyDetails = new HashMap();
                    messageBodyDetails.put("All Messages/" + messagePushID, messageTextBody);

                    GroupNameRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {

                            if (task.isSuccessful()) { } else {

                                Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                            }
                            userMessageInput.setText("");
                        }
                    });


                } else {
                    Toast.makeText(ChatActivity.this, "Failed to send the GIF Image.", Toast.LENGTH_SHORT).show();
                }


            }

        });

    }

    private boolean writeToFileFromContentUri(File file, Uri uri) {

        if (file == null || uri == null) return false;
        try {
            InputStream stream = getContentResolver().openInputStream(uri);
            OutputStream output = new FileOutputStream(file);
            if (stream == null) return false;
            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = stream.read(buffer)) != -1) output.write(buffer, 0, read);
            output.flush();
            output.close();
            stream.close();
            return true;
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "Couldn't open stream: " + e.getMessage());
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException on stream: " + e.getMessage());
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == 438 && resultCode == RESULT_OK)
        {
            loadingBar.setTitle("Sending File");
            loadingBar.setMessage("Please wait, we are sending that file...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            if(checker.equals("pdf") || checker.equals("docx")) {

                if(data!=null && data.getData()!=null)
                {
                    fileUri = data.getData();
                }

                UploadReferenceToFirebaseStorage();

                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        if (taskSnapshot.getMetadata() != null) {
                            Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    docUri = uri.toString();

                                    SaveMessageInfoToDatabase();
                                }
                            });

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.dismiss();
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double p = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        loadingBar.setMessage((int) p + " % Uploading....");
                    }
                });
            }
            else if (checker.equals("gallery")||checker.equals("camera")) {
               /* if (data!=null && data.getData()!=null){
                type = data.getData().toString(); // this error*/
                UploadReferenceToFirebaseStorage();

                if (checker.equals("gallery") && data != null && data.getData() != null) {
                    fileUri = data.getData();
                    uploadTask = filePath.putFile(fileUri);
                } else if (checker.equals("camera")) {
                    uploadTask = filePath.putFile(photoURI);
                }

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {

                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            myUri = downloadUri.toString();

                            SaveMessageInfoToDatabase();

                        } else {

                            Toast.makeText(ChatActivity.this, "Failed to send the image.", Toast.LENGTH_SHORT).show();

                        }
                    }

                });
            //}
            }
            else
            {
                loadingBar.dismiss();
                Toast.makeText(ChatActivity.this,"Nothing is selected, Error.",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void SaveMessageInfoToDatabase() {
        if (checker.equals("pdf") || checker.equals("docx") || checker.equals("gallery") || checker.equals("camera"))
        {
            Map messageTextBody = new HashMap();

            if (checker.equals("gallery") || checker.equals("camera"))
            {
                messageTextBody.put("message", myUri);
            }
            else if (checker.equals("pdf") || checker.equals("docx"))
            { messageTextBody.put("message", docUri); }

            messageTextBody.put("type", checker);
            messageTextBody.put("from", userId);
            messageTextBody.put("name", currentUserName);
            messageTextBody.put("to", currentGroupName);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);


            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put("All Messages/" + messagePushID, messageTextBody);

            GroupNameRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if (task.isSuccessful())
                    {
                        loadingBar.dismiss();
                    } else {

                        loadingBar.dismiss();
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    userMessageInput.setText("");
                }
            });
        }
        else
        {
            Toast.makeText(ChatActivity.this,"Error.",  Toast.LENGTH_SHORT).show();
        }
    }

    private void UploadReferenceToFirebaseStorage() {

        if (checker.equals("gallery") || checker.equals("camera"))
        {
            storageReference = FirebaseStorage.getInstance().getReference("Images").child("Image Files");
        }
        else if (checker.equals("pdf") || checker.equals("docx"))
        {
            storageReference = FirebaseStorage.getInstance().getReference("Files");
        }

        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Messages").child("Main Group").child(currentGroupName);
        DatabaseReference userMessageKeyRef = GroupNameRef.push();
        messagePushID = userMessageKeyRef.getKey();

        if (checker.equals("gallery") || checker.equals("camera"))
        {
            filePath = storageReference.child("Main Group").child(currentGroupName).child(messagePushID + "." + "jpg");
        }
        else if (checker.equals("pdf") || checker.equals("docx"))
        {
            filePath = storageReference.child("Main Group").child(currentGroupName).child(messagePushID + "." + checker);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // Get current user's info.
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(ChatActivity.this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ChatActivity.this, MainActivity.class));
        } else {
            //updateUserStatus("online");
            GetUserInfo();
        }
    }

    @Override
    protected void onResume()
    {
        GetUserInfo();
        super.onResume();
    }

    @Override
    protected void onStop()
    {
     super.onStop();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            //updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy()
    {
     super.onDestroy();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            //updateUserStatus("offline");
        }
    }


    private void GetUserInfo() {

        myRef.child("Users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                intent = getIntent();

                if (dataSnapshot.exists()) {

                    currentUserName = dataSnapshot.child("codename").getValue().toString();
                    currentFullName = dataSnapshot.child("fullname").getValue().toString();

                } else {
                    startActivity(new Intent(ChatActivity.this, MainActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        // Set picture
        // Load profile image from firebase database to ImageView imageReference
        imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).fit().centerCrop().into(profilePic);
            }
        });
    }


    public boolean onCreateOptionsMenu (Menu menu){ return true; }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (myToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // A pop-up message will show up.
    private void AddNewGroup()
    {
        ExampleDialog exampleDialog = new ExampleDialog();
        exampleDialog.show(getSupportFragmentManager(),"example dialog");
    }

    // With values String groupName and String type, taken from ExampleDialog
    // when user key-in the info, the group will be created.
    // However, if group name exists in firebase, a Toast message will appear,
    // telling the user to give a different group name,
    // and the group with the same group name that is already in the firebase will not be created.
    @Override
    public void applyTexts(final String groupName, final String type) {

        verify = verify + 1;

        myRef = FirebaseDatabase.getInstance().getReference();
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("Messages")) {

                    databaseReference = myRef.child("Messages").child("Groups");
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for (DataSnapshot child : dataSnapshot.getChildren()) {

                                if (child.getKey() != null ) {

                                    groupChatNames.add(child.getKey());

                                }
                            }

                            if (groupChatNames.contains(groupName)) {

                                if(verify > 1) {
                                    Toast.makeText(ChatActivity.this, "Group name already exists. Please choose another one.", Toast.LENGTH_SHORT).show();
                                    verify = 0;
                                }

                            } else if (!groupChatNames.contains(groupName)) {
                                verify = 0;
                                CreateNewGroup(groupName, type);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }


                    });

                } else {
                    CreateNewGroup(groupName, type);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    // Upload group name to firebase.
    private void CreateNewGroup (final String groupName, final String type){

        check = check + 1;
        myRef = FirebaseDatabase.getInstance().getReference();
       DatabaseReference reference = myRef.child("Users").child(userId);

       reference.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

               fullname = dataSnapshot.child("fullname").getValue().toString();
               codename = dataSnapshot.child("codename").getValue().toString();

               myRef.child("Messages").child("Groups").child(groupName).child("Group Members").child("Owner").child(fullname).child("codename").setValue(codename)
                       .addOnCompleteListener(new OnCompleteListener<Void>() {
                           @Override
                           public void onComplete(@NonNull Task<Void> task) {
                               myRef.child("Messages").child("Groups").child(groupName).child("Group Members").child("Members").child(fullname).child("codename").setValue(codename);
                               myRef.child("Messages").child("Groups").child(groupName).child("Group Members").child("Members").child(fullname).child("status").setValue("Owner");
                               myRef.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                                   @Override
                                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                       for (DataSnapshot ds : dataSnapshot.getChildren())
                                       {
                                           String userfullname = ds.child("fullname").getValue().toString();
                                           if (userfullname.equals(fullname))
                                           {
                                               String userid = ds.getKey();
                                               myRef.child("Users").child(userid).child("Groups").child(groupName).setValue("Owner");
                                           }
                                       }
                                   }

                                   @Override
                                   public void onCancelled(@NonNull DatabaseError databaseError) {

                                   }
                               });


                               if (task.isSuccessful()) {
                                   if (check >0)
                                   {
                                   Toast.makeText(ChatActivity.this, groupName + " is created successfully.", Toast.LENGTH_SHORT).show();
                                   check = 0;}

                               }
                               else{ check = 0;}
                           }
                       });


               if (type.equals("Private"))
               { }
               else if (type.equals("Public"))
               {
                   myRef.addValueEventListener(new ValueEventListener() {
                       @Override
                       public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                       {

                           if (dataSnapshot.hasChild("Users"))
                           {
                               databaseReference = myRef.child("Users");
                               databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                   @Override
                                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                       for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                           if (ds.child("fullname").getValue(String.class) != null ) {

                                               userFullnameForGroupChat = ds.child("fullname").getValue(String.class);
                                               userCodenameForGroupChat = ds.child("codename").getValue(String.class);
                                               String userIdForGroupChat = ds.getKey();



                                               if (userFullnameForGroupChat.contains(fullname))
                                               {

                                               } else if (!userFullnameForGroupChat.contains(fullname)) {

                                                   myRef.child("Messages").child("Groups").child(groupName).child("Group Members").child("Members").child(userFullnameForGroupChat).child("codename").setValue(userCodenameForGroupChat);
                                                   myRef.child("Messages").child("Groups").child(groupName).child("Group Members").child("Members").child(userFullnameForGroupChat).child("status").setValue("Member");
                                                   myRef.child("Users").child(userIdForGroupChat).child("Groups").child(groupName).setValue("Member");

                                               }
                                           }
                                       }
                                   }

                                   @Override
                                   public void onCancelled(@NonNull DatabaseError databaseError) {

                                   }
                               });
                           }
                       }

                       @Override
                       public void onCancelled(@NonNull DatabaseError databaseError) {

                       }
                   });
               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });


    }



    // Open and close the drawer.
    @Override
    public void onBackPressed(){
        if(myDrawer.isDrawerOpen(GravityCompat.START)){
            // Option to close drawer
            myDrawer.closeDrawer(GravityCompat.START);

        }else{
            Intent intent = new Intent(ChatActivity.this,MenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            Log.e(LOG_TAG,"Back");
            finish();
        }
    }

    private void setupUIViews() {

        // Declaration of elements.

        sendButton = (findViewById(R.id.btnSend));
        clipButton = (findViewById(R.id.btnclip));
        cameraButton = (findViewById(R.id.camera));
        microphoneButton = (findViewById(R.id.microphone));
        myDrawer = (findViewById(R.id.myDrawer));
        toolbar = (findViewById(R.id.toolbar));
        NavigationView =(findViewById(R.id.navigation_view));
        header = NavigationView.getHeaderView(0);
        profilePic = header.findViewById(R.id.profile_pic_2);

        userMessageInput = (findViewById(R.id.etMessage));

        chatAdapter = new MainChatAdapter(getApplicationContext(),messageList);
        rvMessage = (findViewById(R.id.rvMessage));
        linearLayoutManager = new LinearLayoutManager(this);
        rvMessage.setLayoutManager(linearLayoutManager);
        rvMessage.setAdapter(chatAdapter);

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());


    }
    @Override
    protected void onPause(){
        super.onPause();
    }
    private void subscribeToNotification(String groupName) {
        FirebaseMessaging.getInstance().subscribeToTopic(groupName)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = getString(R.string.msg_subscribed);
                        if (!task.isSuccessful()) {
                            msg = getString(R.string.msg_subscribe_failed);
                        }
                        Log.d(LOG_TAG, msg);
                        Toast.makeText(ChatActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void sendNotification(String fullname, final String codename, final String messageInput) {

        Message data = new Message(userId,codename + ": " + messageInput,"text",currentGroupName,messagePushID,saveCurrentTime, saveCurrentDate, codename);
        String msg=messageInput;
        if(messageInput.length()>51){
            msg = messageInput.substring(0,50)+"...";
        }
        Notification notification = new Notification(msg,currentGroupName,".PrivateGroupChatActivity");
        Sender sender = new Sender(data, "/topics/"+getString(R.string.main_chat)+currentGroupName,notification); // Need to change to get token from all users except the sender
        // The current one is sender get own notification

        apiService.sendNotification(sender)
                .enqueue(new Callback<Response>() {
                    @Override
                    public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {

                        Log.e("TAG_MainGroup",String.valueOf(response.isSuccessful()));



                    }

                    @Override
                    public void onFailure(Call<Response> call, Throwable t) {

                    }
                });

    }
}
