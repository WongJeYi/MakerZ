package com.makerz;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.makerz.model.GroupMembers;
import com.makerz.model.user;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;

// This activity is used to edit info about the current user.

public class EditActivity extends AppCompatActivity {

    String userId;
    String codename,name,email;
    private int success = 0;
    private static final int IMAGE_REQUEST = 1;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private ImageView profilePic;
    private TextView Fullname;
    private TextView Codename;
    private Button Update;
    private Button Back;

    private Uri imageUri;
    private ProgressDialog progressDialog;
    private DatabaseReference myRef;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // Declaration of elements.
        setupUIViews();

        // Request for gallery permission.
        if ( ContextCompat.checkSelfPermission( EditActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( EditActivity.this, new String[] {  Manifest.permission.READ_EXTERNAL_STORAGE },
                    001 );
        }
        SharedPreferences spref = PreferenceManager.getDefaultSharedPreferences(this);

// Creating an Editor object to edit(write to the file)
        SharedPreferences.Editor myEdit = spref.edit();

        if (spref.getBoolean("FirstTimeEdit", true)) {
            myEdit.putBoolean("FirstTimeEdit", false);
            myEdit.commit();
            newUser();
        }
        // Get current user's info.
        // If cannot retrieve info, will move to MainActivity which is the login page.
        firebaseDatabase = FirebaseDatabase.getInstance();

        storageReference = FirebaseStorage.getInstance().getReference("Images");

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = firebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            Toast.makeText(EditActivity.this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(EditActivity.this, MainActivity.class));
        }

        // Set picture
        myRef = firebaseDatabase.getReference().child("Users").child(userId);
        StorageReference imageReference = storageReference.child("Profile Picture").child(userId + "/" + "profilepic.jpg");

        // Load profile image from firebase database to ImageView imageReference
        imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).fit().centerCrop().into(profilePic);
            }
        });

        // Listen to current user's data values.
        // When Button Update is pressed, any changed info will be updated to firebase,
        // else nothing happens.
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                Codename.setText(dataSnapshot.child("codename").getValue(String.class));
                Fullname.setText(dataSnapshot.child("fullname").getValue(String.class));
                final String oldCodename=dataSnapshot.child("codename").getValue(String.class);
                final String oldFullname=dataSnapshot.child("fullname").getValue(String.class);

                Update.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        UpdateProfile(oldCodename,oldFullname);
                    }

                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EditActivity.this, databaseError.getCode(), Toast.LENGTH_SHORT).show();
            }


        });

        // When ImageView profilePic is clicked, gallery will open.
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImage();

            }
        });

        // When Button Back is clicked, will move to ProfileActivity.
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void UpdateProfile(final String oldFullname,final String oldCodename) {

        StorageReference imageReference = storageReference.child("Profile Picture").child(userId + "/" + "profilepic.jpg");
        codename = Codename.getText().toString();
        firebaseAuth=FirebaseAuth.getInstance();
        email = firebaseAuth.getCurrentUser().getEmail();
        name = Fullname.getText().toString();
        progressDialog = new ProgressDialog(EditActivity.this);
        progressDialog.setMessage("Uploading data...");
        progressDialog.show();


        // If profilePicture is changed, the uri in the firebase of user's profile picture will be changed.

        if (success == 1)
        {
            imageReference.putFile(imageUri);
            success = 0;
        }

        myRef.child("codename").setValue(codename);
        myRef.child("fullname").setValue(name);
        myRef.child("email").setValue(email);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot s:snapshot.getChildren()){
                        final DatabaseReference group=databaseReference.child(s.getKey()).child("Group Members").child("Members");
                        group.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    for(DataSnapshot ds:snapshot.getChildren()){
                                        if(ds.getKey().equals(oldFullname)) {
                                            final GroupMembers old = ds.getValue(GroupMembers.class);
                                            ds.getRef().removeValue();
                                            ds.getRef().addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    GroupMembers members=new GroupMembers(codename,old.getStatus());
                                                    group.child(name).setValue(members);
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });

                                        }

                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
                Toast.makeText(EditActivity.this, "Upload successful.", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void newUser() {
        TapTargetView.showFor(EditActivity.this, TapTarget.forView(profilePic, "This is Profile Picture button", "You can add your profile picture here")
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
                .tintTarget(false)
                .dimColor(R.color.colorPrimary)
                .targetRadius(120)
                .transparentTarget(true), new TapTargetView.Listener() {
            @Override
            public void onTargetClick(TapTargetView view) {
                super.onTargetClick(view);
                view.dismiss(true);
                openImage();
            }
        });
    }

    // Will only show gallery, due to intent.setType("image/*");
    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*"); // application/* OR audio/* OR pdf/*
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select image."), IMAGE_REQUEST);
    }

    // Needed to request open gallery of user's phone.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data !=null && data.getData() != null)
        {
            imageUri = data.getData();
            try {
                InputStream is = getContentResolver().openInputStream(imageUri);
                final Bitmap selected_image = BitmapFactory.decodeStream(is);
                profilePic.setImageBitmap(selected_image);
                success = 1;
                SharedPreferences spref = PreferenceManager.getDefaultSharedPreferences(this);

                // Creating an Editor object to edit(write to the file)
                SharedPreferences.Editor myEdit = spref.edit();

                if (spref.getBoolean("FirstTimeEditA", true)) {
                    myEdit.putBoolean("FirstTimeEditA", false);
                    myEdit.commit();
                    TapTargetView.showFor(EditActivity.this, TapTarget.forView(Update, "This is Update button", "You can update your profile by clicking here")
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
                            startActivity(new Intent(EditActivity.this,MenuActivity.class));
                            finish();
                        }
                    });
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setupUIViews() {

        profilePic = (ImageView) findViewById(R.id.profile_pic);
        Fullname = (TextView) findViewById(R.id.name_1);
        Codename = (TextView) findViewById(R.id.codename_1);
        Update = (Button) findViewById(R.id.button_update);
        Back = (Button) findViewById(R.id.button_back3);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Messages").child("Groups");

    }
}



