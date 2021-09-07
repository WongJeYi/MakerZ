package com.makerz;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.ClipDescription;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;
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
import com.makerz.Notifications.Token;
import com.makerz.model.Message;
import com.makerz.adapter.ChatAdapter;
import com.makerz.model.user;
import com.makerz.util.LoadingDialog;

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

import retrofit2.Call;
import retrofit2.Callback;

// This activity is used for users to chat with each other in a specfic private/public group.

public class PrivateGroupChatActivity extends AppCompatActivity {

    // After checking if FCM is working, things that need to move to ChatActivity
    // 1. Update token    2. onResume    3. updateToken(String token)

    // 5. Maybe delete MyFirebaseMessaging.java & GettingDeviceTokenService.java under Notifications

    // Initialize variables
    private final List<Message> messageList = new ArrayList<>();
    private String messageReceiverID, messageReceiverName, messageReceiverImage;
    private String messagePushID; //, messageInput;
    private String currentGroupName, userId, currentUserName,  saveCurrentDate, saveCurrentTime, currentFullName;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference myRef, GroupNameRef;
    private StorageReference imageReference;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser mFirebaseUser;

    private LinearLayoutManager linearLayoutManager;
    private ChatAdapter chatAdapter;
    RecyclerView rvMessage;
    Intent intent;

    private RichContentEditText userMessageInput;
    private ImageButton sendButton;
    private ImageButton clipButton;
    private ImageButton microphoneButton;
    private ImageButton cameraButton;

    StorageReference filePath, audiofilePath, giffilePath;
    StorageReference storageReference;
    Uri fileUri;

    final LoadingDialog loadingDialog = new LoadingDialog(PrivateGroupChatActivity.this);

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
    boolean notify = false;
    private NotificationManagerCompat notificationManager;
    private String[] retImage = {"default_image"};
    private user currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_group_chat);
        setupUIViews();
        Log.e(LOG_TAG,"create");
      /*  // Request for camera permission.
        if ( ContextCompat.checkSelfPermission( PrivateGroupChatActivity.this, Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( PrivateGroupChatActivity.this, new String[] {  Manifest.permission.CAMERA  },
                    438 );
        }

        // Request for audio permission.
        if ( ContextCompat.checkSelfPermission( PrivateGroupChatActivity.this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( PrivateGroupChatActivity.this, new String[] {  Manifest.permission.RECORD_AUDIO  },
                    000 );

        }*/

        // Set for notification.
        //notificationManager = NotificationManagerCompat.from(PrivateGroupChatActivity.this);

        // To set the textbox on top of the keyboard.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        new DownloadTask().execute();

        // Declaration of elements.
        loadingBar = new ProgressDialog(PrivateGroupChatActivity.this);

        firebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = firebaseAuth.getCurrentUser();
        userId = firebaseAuth.getCurrentUser().getUid();
        updateUser();

    }
    private void setup(){

        final StorageReference[] storageReference = {FirebaseStorage.getInstance().getReference("Images")};
        firebaseDatabase = FirebaseDatabase.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference();
        imageReference = storageReference[0].child("Profile Picture").child(userId + "/" + "profilepic.jpg");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Messages").child("Groups").child(currentGroupName);
        setupDatabase();
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

                        Toast.makeText(PrivateGroupChatActivity.this,
                                "Failed to send the GIF Image.", Toast.LENGTH_LONG).show();
                    } else {
                        // Hide keyboard after successfully sending message.
                        View view = PrivateGroupChatActivity.this.getCurrentFocus();

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

                notify = true;

                // Hide keyboard after successfully sending message.
                View view = PrivateGroupChatActivity.this.getCurrentFocus();

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

                AlertDialog.Builder builder = new AlertDialog.Builder(PrivateGroupChatActivity.this);
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
                        {"Gallery",
                                "Camera"
                        };

                AlertDialog.Builder builder = new AlertDialog.Builder(PrivateGroupChatActivity.this);
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
                    Toast.makeText(PrivateGroupChatActivity.this,"Recording started...",Toast.LENGTH_SHORT).show();

                } else  if (event.getAction() == MotionEvent.ACTION_MOVE){

                    if(event.getEventTime() - event.getDownTime() > 500){
                        longClick = true;
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP){

                    if(longClick){
                        stopRecording();
                        Toast.makeText(PrivateGroupChatActivity.this,"Recording stopped...",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(PrivateGroupChatActivity.this,"Please long-press on the button to record audio.",Toast.LENGTH_SHORT).show();

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

        // Create API Service
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);
        loadingBar.dismiss();
    }
    @Override
    protected void onResume()
    {
        Log.e(LOG_TAG,"Resume");
       super.onResume();
    }

    private void updateToken(String token) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(currentFullName).setValue(mToken);
    }
    private void setupDatabase(){

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    FirebaseDatabase.getInstance().getReference().setValue("Messages");
                    FirebaseDatabase.getInstance().getReference().child("Messages").setValue("Groups");
                    FirebaseDatabase.getInstance().getReference().child("Messages").child("Groups").setValue(currentGroupName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public boolean writeToFileFromContentUri(File file, Uri uri) {
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
    public void onBackPressed(){
        Intent intent = new Intent(PrivateGroupChatActivity.this,ViewGroupActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        Log.e(LOG_TAG,"Back");
        finish();
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

        try {

            if(recorder != null && isRecording){
            recorder.stop();
            recorder.release();
            recorder = null;
            SaveMessageInfoToDatabaseForAudio();}

        } catch (IllegalStateException e) {
            Log.e(LOG_TAG, "stop() failed");

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
                Toast.makeText(PrivateGroupChatActivity.this, "Error", Toast.LENGTH_SHORT).show();

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
                            Toast.makeText(PrivateGroupChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

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

                            Toast.makeText(PrivateGroupChatActivity.this, "Failed to send the image.", Toast.LENGTH_SHORT).show();

                        }
                    }

                });
            }
            }
            else
                {
                    loadingBar.dismiss();
                   Toast.makeText(PrivateGroupChatActivity.this,"Nothing is selected, Error.",Toast.LENGTH_SHORT).show();
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

        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Messages").child("Groups").child(currentGroupName);
        DatabaseReference userMessageKeyRef = GroupNameRef.push();//child(userId).child(messageReceiverID).push();
        messagePushID = userMessageKeyRef.getKey();

        if (checker.equals("gallery") || checker.equals("camera"))
        {
            filePath = storageReference.child(currentGroupName).child(messagePushID + "." + "jpg");
        }
        else if (checker.equals("pdf") || checker.equals("docx"))
        {
            filePath = storageReference.child(currentGroupName).child(messagePushID + "." + checker);
        }
    }

    @Override
    protected void onDestroy(){
        Log.e(LOG_TAG,"destroy");
        super.onDestroy();
    }
    private void SaveMessageInfoToDatabaseForText() {
        final String messageInput = userMessageInput.getText().toString();

        if (!messageInput.equals(""))
        {
            //String messageSenderRef = "All Messages/" + userId + "/" + messageReceiverID;
            //String messageReceiverRef = "All Messages/" + messageReceiverID + "/" + userId;

            GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Messages").child("Groups").child(currentGroupName);
            DatabaseReference userMessageKeyRef = GroupNameRef.push();//child(userId).child(messageReceiverID).push();

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
            messageBodyDetails.put("All Messages/" + messagePushID, messageTextBody);  //messageSenderRef + "/" + messagePushID, messageTextBody);
            //    messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

            GroupNameRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if (task.isSuccessful())
                    {

                        if (notify&& currentUser!=null)
                        {
                            // Getting through here
                            sendNotification(currentUser.getFullname(), currentUser.getCodename(),messageInput);
                        }
                        notify = false;
                    }
                    else
                    {
                        Toast.makeText(PrivateGroupChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
        else
        {
            Toast.makeText(PrivateGroupChatActivity.this,"You can't send an empty message.",  Toast.LENGTH_SHORT).show();
        }

    }

    private void sendNotification(String fullname, final String codename, final String messageInput) {

                    Message data = new Message(userId,codename + ": " + messageInput,"text",currentGroupName,messagePushID,saveCurrentTime, saveCurrentDate, codename);
                    String msg=messageInput;
                    if(messageInput.length()>51){
                        msg = messageInput.substring(0,50)+"...";
                    }
                    Notification notification = new Notification(msg,currentGroupName,".PrivateGroupChatActivity");
                    Sender sender = new Sender(data, "/topics/"+getString(R.string.chat)+currentGroupName,notification); // Need to change to get token from all users except the sender
                                                                        // The current one is sender get own notification

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {

                                    Log.e("TAG_PrivateGroup",String.valueOf(response.isSuccessful()));



                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {

                                }
                            });

    }

    private void SaveMessageInfoToDatabaseForGif(){

       storageReference = FirebaseStorage.getInstance().getReference("GIF Images");
       GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Messages").child("Groups").child(currentGroupName);
        DatabaseReference userMessageKeyRef = GroupNameRef.push();
        messagePushID = userMessageKeyRef.getKey();

        Uri gifUri = Uri.fromFile(richContentFile);

        giffilePath = storageReference.child(currentGroupName).child(messagePushID+ "." + "gif");
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

                                Toast.makeText(PrivateGroupChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                            }
                            userMessageInput.setText("");
                        }
                    });


                } else {
                    Toast.makeText(PrivateGroupChatActivity.this, "Failed to send the GIF Image.", Toast.LENGTH_SHORT).show();
                }


            }

       });


    }

    private void SaveMessageInfoToDatabaseForAudio() {

        loadingBar.setTitle("Sending File");
        loadingBar.setMessage("Please wait, we are sending that file...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        storageReference = FirebaseStorage.getInstance().getReference("Files");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Messages").child("Groups").child(currentGroupName);
        DatabaseReference userMessageKeyRef = GroupNameRef.push();
        messagePushID = userMessageKeyRef.getKey();

        Uri audioUri = Uri.fromFile(new File(fileName));

        audiofilePath = storageReference.child(currentGroupName).child(messagePushID+ "." + "3gp");
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
                                Toast.makeText(PrivateGroupChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                            }
                            userMessageInput.setText("");
                        }
                    });


                } else {
                    Toast.makeText(PrivateGroupChatActivity.this, "Failed to send the audio.", Toast.LENGTH_SHORT).show();
                }

            }

        });


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
                            Toast.makeText(PrivateGroupChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                        userMessageInput.setText("");
                    }
                });
        }
        else
        {
            Toast.makeText(PrivateGroupChatActivity.this,"Error.",  Toast.LENGTH_SHORT).show();
        }
    }


    private void GetUserInfo() {

        // Token ID is id for the phone, each phone has its own unique ID
        // Update token ID if user use another phone to login

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseInstallations.getInstance().getToken(true).addOnCompleteListener(new OnCompleteListener<InstallationTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<InstallationTokenResult> task) {
                if (user != null)
                {
                    firebaseAuth = FirebaseAuth.getInstance();
                    mFirebaseUser = firebaseAuth.getCurrentUser();
                    userId = firebaseAuth.getCurrentUser().getUid();
                    final String tokenRefresh =  task.getResult().getToken();

                    if (getIntent() != null && getIntent().getExtras() != null)
                    {
                        // Save uid of currently signed in user in shared preferences
                        SharedPreferences sp = getSharedPreferences("SP_GROUPNAME", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("Current_GROUPNAME", currentGroupName);
                        editor.apply();

                    }



                    myRef.child("Users").child(userId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            intent = getIntent();

                            if(dataSnapshot.exists()){

                                currentUserName = dataSnapshot.child("codename").getValue().toString();
                                currentFullName = dataSnapshot.child("fullname").getValue().toString();

                                updateToken(tokenRefresh);

                            }
                            else
                            {
                                startActivity(new Intent(PrivateGroupChatActivity.this, MainActivity.class));
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }

        });


    }


    // AsyncTask is a background task that runs in the background thread usually used for loading pictures, videos & images
    // AsyncTask <Input, Progress, Output>

    private class DownloadTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected  void onPreExecute(){
            // Showing progress dialog
            super.onPreExecute();

            loadingDialog.startLoadingDialog();

        }
        protected Void doInBackground(Void... params) {

            // Here you are in the worker thread and you are not allowed to access UI thread from here.
            // Here you can perform network operations or any heavy operations you want.

            // Wait for 2.5 seconds before closing the loading circle dialog
            try{
                Thread.sleep(2500);
            }catch (Exception e)
            {
                e.printStackTrace();
            }

            return null;
        }
        protected void onProgressUpdate(Void aVoid){

            // You can track your progress update here.

        }
        protected void onPostExecute(Void aVoid){
            // After completing execution of given task, control will return here.
            // Hence, if you want to populate UI elements with fetched data,do it here.
            super.onPostExecute(aVoid);

            if (loadingDialog!=null )
            {
                loadingDialog.dismissDialog();
            }


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Inflate the menu; this adds item to the action bar if it exist.

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu1,menu);

        return true;
    }

    // If item1 on toolbar is pressed, pass user's values and move to ViewGroupMembers activity.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.item1:

                if (getIntent() != null && getIntent().getExtras() != null)
                {
                    currentGroupName = getIntent().getExtras().get("groupName").toString();
                    messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
                    messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
                    messageReceiverImage = getIntent().getExtras().get("visit_image").toString();

                    Intent groupChatIntent = new Intent(PrivateGroupChatActivity.this, ViewGroupMembers.class);
                    groupChatIntent.putExtra("groupName", currentGroupName);
                    groupChatIntent.putExtra("visit_user_id",  messageReceiverID);
                    groupChatIntent.putExtra("visit_user_name",  messageReceiverName);
                    groupChatIntent.putExtra("visit_image", messageReceiverImage);
                    startActivity(groupChatIntent);
                    finish();
                }

                return true;

            case R.id.item2:
                // Returns nothing
                return true;

                default: return super.onOptionsItemSelected(item);



        }
    }

    private void setupUIViews() {

        // Declaration of elements.
        userMessageInput = (findViewById(R.id.etMessage1));

        sendButton = (findViewById(R.id.btnSend1));
        clipButton = (findViewById(R.id.btnclip1));
        cameraButton = (findViewById(R.id.camera1));
        microphoneButton = (findViewById(R.id.microphone1));

        chatAdapter = new ChatAdapter(getApplicationContext(),messageList);
        rvMessage = (findViewById(R.id.rvMessage1));
        linearLayoutManager = new LinearLayoutManager(this);
        rvMessage.setLayoutManager(linearLayoutManager);
        rvMessage.setAdapter(chatAdapter);

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

    }
    private void updateUser(){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                currentUser  = dataSnapshot.getValue(user.class);

                               if (dataSnapshot.hasChild("Images")) {
                                    retImage[0] = dataSnapshot.child("Images").getValue().toString();
                                }
                if (getIntent() != null && getIntent().getExtras() != null)
                {
                    if(getIntent().getExtras().containsKey("groupName")) {
                        currentGroupName = getIntent().getExtras().get("groupName").toString();
                        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
                        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
                        messageReceiverImage = getIntent().getExtras().get("visit_image").toString();

                    }else {
                        Bundle bundle = getIntent().getExtras();
                        currentGroupName = bundle.get("Receiver").toString();
                        messageReceiverID=userId;
                        messageReceiverName=currentUser.getCodename();
                        messageReceiverImage = retImage[0];

                    }

                }
                PrivateGroupChatActivity.this.setTitle(currentGroupName);
                setup();



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
